package com.zhuimeng.qcmanagement.service.impl;

import com.zhuimeng.Result.Result;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.ContractMapper;
import com.zhuimeng.qcmanagement.service.ContractService;
import com.zhuimeng.vo.ContractVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {
    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    // 合同列表key
    private static final String key = RedisKeyProperties.CONTRACT_LIST;

    private static final Object roomListLock = new Object(); // 独立的锁对象

    /** 合同分页查询（Redis缓存全量，内存过滤+分页）
     * */
    @Override
    public Result contractList(Integer pageNum, Integer pageSize, String contractNo, String elderName, Integer status, Long startTime, Long endTime) {
        // 时间戳转 LocalDateTime
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        if (startTime != null) {
            startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
        }
        if (endTime != null) {
            endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());
        }

        // 获取全量合同列表（优先Redis）
        List<ContractVO> allContracts = getAllContractsFromCache();

        // 内存过滤
        List<ContractVO> filteredList = filterContracts(allContracts, contractNo, elderName, status, startDateTime, endDateTime);

        // 内存分页
        int total = filteredList.size();
        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<ContractVO> pageRecords = (fromIndex < total)
                ? filteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("records", pageRecords);
        data.put("pages", pages);
        data.put("total", total);

        return Result.ok(data);
    }

    /** 从Redis获取全量合同列表，缓存未命中则查DB
     * */
    private List<ContractVO> getAllContractsFromCache() {
        // 1.查询Redis
        List<ContractVO> contracts = (List<ContractVO>) redisTemplate.opsForValue().get(key);

        // 2.判断Redis是否存储
        if (contracts != null && !contracts.isEmpty()) {
            return contracts;
        }

        // 本地锁（防止缓存击穿）
        synchronized (roomListLock) {
            // 3.再次查询Redis
            contracts = (List<ContractVO>) redisTemplate.opsForValue().get(key);
            if (contracts != null && !contracts.isEmpty()) {
                return contracts;
            }

            // 4.查询数据库（不带筛选条件，获取全量数据）
            contracts = contractMapper.selectContractVOList(null, null, null, null, null);

            // 5.存储Redis
            redisTemplate.opsForValue().set(key, contracts, 1, TimeUnit.HOURS);
        }

        return contracts;
    }

    /** 内存过滤
     * */
    private List<ContractVO> filterContracts(List<ContractVO> list, String contractNo, String elderName, Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        return list.stream()
                .filter(c -> contractNo == null || contractNo.isEmpty() || (c.getContractNo() != null && c.getContractNo().contains(contractNo)))
                .filter(c -> elderName == null || elderName.isEmpty() || (c.getElderName() != null && c.getElderName().contains(elderName)))
                .filter(c -> status == null || (c.getStatus() != null && c.getStatus().equals(status)))
                .filter(c -> startTime == null || (c.getStartTime() != null && !c.getStartTime().isBefore(startTime)))
                .filter(c -> endTime == null || (c.getEndTime() != null && !c.getEndTime().isAfter(endTime)))
                .collect(Collectors.toList());
    }

    /** 合同详情（含老人信息、房间床位、护理等级、合同日期、PDF地址）
     * */
    @Override
    public Result contractDetail(Long id) {
        if (id == null) {
            return Result.fail("合同ID不能为空");
        }

        // 查询Redis缓存
        String cacheKey = key + "detail:" + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Result.ok(cached);
        }

        Map<String, Object> detail = contractMapper.selectContractDetailById(id);
        if (detail == null) {
            return Result.fail("合同不存在");
        }

        // 组装嵌套 elderVo（前端取值 elderVo.name / elderVo.idCardNo）
        Map<String, Object> elderVo = new HashMap<>();
        elderVo.put("id", detail.remove("elderId"));
        elderVo.put("name", detail.remove("elderName"));
        elderVo.put("idCardNo", detail.remove("elderIdCardNo"));
        elderVo.put("age", detail.remove("elderAge"));
        elderVo.put("sex", detail.remove("elderSex"));
        elderVo.put("phone", detail.remove("elderPhone"));
        elderVo.put("image", detail.remove("elderImage"));
        elderVo.put("bedNumber", detail.remove("bedNumber"));
        elderVo.put("bedId", detail.remove("bedId"));
        detail.put("elderVo", elderVo);

        // 组装嵌套 nursingLevelVo
        Map<String, Object> nursingLevelVo = new HashMap<>();
        nursingLevelVo.put("name", detail.remove("nursingLevelName"));
        nursingLevelVo.put("fee", detail.remove("nursingLevelFee"));
        detail.put("nursingLevelVo", nursingLevelVo);

        // 缓存（5分钟）
        redisTemplate.opsForValue().set(cacheKey, detail, 5, TimeUnit.MINUTES);

        return Result.ok(detail);
    }
}
