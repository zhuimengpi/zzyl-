package com.zhuimeng.qcmanagement.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zhuimeng.Result.Result;
import com.zhuimeng.Thread.UserHolder;
import com.zhuimeng.dto.*;
import com.zhuimeng.entity.CheckIn;
import com.zhuimeng.entity.Bill;
import com.zhuimeng.entity.Elder;
import com.zhuimeng.entity.Member;
import com.zhuimeng.entity.MemberElder;
import com.zhuimeng.properties.RedisKeyProperties;
import com.zhuimeng.qcmanagement.mapper.BillMapper;
import com.zhuimeng.qcmanagement.mapper.CheckInMapper;
import com.zhuimeng.qcmanagement.mapper.FamilyMapper;
import com.zhuimeng.qcmanagement.service.CheckInService;
import com.zhuimeng.util.OrderNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 入住管理 Service 实现
* */
@Service
public class CheckInServiceImpl implements CheckInService {
    @Autowired
    private CheckInMapper checkInMapper;
    @Autowired
    private FamilyMapper familyMapper;
    @Autowired
    private BillMapper billMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    // 入住申请列表缓存key
    private static final String CHECK_IN_LIST_KEY = RedisKeyProperties.CHECK_IN_LIST;

    // ==================== V2 ====================

    /** 分页查询入住申请列表 */
    @Override
    public Result pageQuery(Integer pageNum, Integer pageSize, Integer status,
                            String elderName, String startTime, String endTime) {
        // 无筛选时尝试 Redis
        boolean noFilter = status == null
                && (elderName == null || elderName.isEmpty())
                && (startTime == null || startTime.isEmpty())
                && (endTime == null || endTime.isEmpty());

        if (noFilter) {
            String cacheKey = CHECK_IN_LIST_KEY + pageNum + ":" + pageSize;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Result.ok(cached);
            }
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = checkInMapper.selectPageQuery(status, elderName, startTime, endTime);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        if (noFilter) {
            String cacheKey = CHECK_IN_LIST_KEY + pageNum + ":" + pageSize;
            redisTemplate.opsForValue().set(cacheKey, data, 5, java.util.concurrent.TimeUnit.MINUTES);
        }

        return Result.ok(data);
    }

    /** 提交入住申请 */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result apply(CheckInApplyDTO dto) {
        CheckInElderDTO elderDto = dto.getCheckInElderDto();
        CheckInConfigDTO configDto = dto.getCheckInConfigDto();
        CheckInContractDTO contractDto = dto.getCheckInContractDto();

        if (elderDto == null || elderDto.getName() == null || elderDto.getName().isBlank()) {
            return Result.fail("老人姓名不能为空");
        }
        if (elderDto.getIdCardNo() == null || elderDto.getIdCardNo().isBlank()) {
            return Result.fail("身份证号不能为空");
        }

        // 1. 查询或创建老人
        Elder elder = familyMapper.selectByIdCard(elderDto.getIdCardNo());
        boolean isNewElder = false;
        if (elder == null) {
            isNewElder = true;
            elder = new Elder();
            elder.setName(elderDto.getName());
            elder.setIdCardNo(elderDto.getIdCardNo());
            elder.setPhone(elderDto.getPhone());
            elder.setSex(elderDto.getSex());
            elder.setAge(elderDto.getAge() != null ? String.valueOf(elderDto.getAge()) : null);
            familyMapper.insert(elder);
            String elderId = familyMapper.selectId(elderDto.getIdCardNo());
            elder.setId(Long.valueOf(elderId));
        } else {
            // 老人已存在，更新基本信息并改为入住中
            familyMapper.updateElderInfo(
                    elder.getId(),
                    elderDto.getName() != null ? elderDto.getName() : elder.getName(),
                    elderDto.getPhone() != null ? elderDto.getPhone() : elder.getPhone(),
                    elderDto.getSex() != null ? elderDto.getSex() : elder.getSex(),
                    elderDto.getAge() != null ? String.valueOf(elderDto.getAge()) : elder.getAge(),
                    4  // status=4 入住中
            );
        }

        Long elderId = elder.getId();
        String userId = UserHolder.getId();

        // 1.1 新老人初始化余额账户
        if (isNewElder) {
            familyMapper.insertBalance(elderId, elderDto.getName());
        }

        // 2. 更新老人床位信息
        familyMapper.updateElderBed(elderId, configDto.getCode(), configDto.getBedId());

        // 3. 插入家属信息
        Long primaryMemberId = null;
        String primaryMemberName = null;
        List<ElderFamilyDTO> familyList = dto.getElderFamilyDtoList();
        // 查询该老人是否已有 member_elder 关联
        MemberElder existingMemberElder = familyMapper.selectMemberElder(elderId);
        boolean elderHasRelation = existingMemberElder != null;

        if (familyList != null && !familyList.isEmpty()) {
            for (ElderFamilyDTO familyDto : familyList) {
                if (familyDto.getPhone() == null || familyDto.getPhone().isBlank()) {
                    continue;
                }

                // 3.1 查询或创建家属
                Member member = familyMapper.selectMemberByPhone(familyDto.getPhone());
                if (member == null) {
                    member = new Member();
                    member.setPhone(familyDto.getPhone());
                    member.setName(familyDto.getName());
                    familyMapper.insertMember(member);
                }

                // 3.2 记录第一个家属作为申请人
                if (primaryMemberId == null) {
                    primaryMemberId = member.getId();
                    primaryMemberName = member.getName();
                }

                // 3.3 插入老人与家属关联（已存在则跳过）
                if (!elderHasRelation) {
                    MemberElder memberElder = new MemberElder();
                    memberElder.setElderId(elderId);
                    memberElder.setMemberId(member.getId());
                    familyMapper.insertMemberElder(memberElder);
                    elderHasRelation = true; // 只插入一次
                }
            }
        }

        // 4. 插入入住申请
        CheckIn checkIn = new CheckIn();
        checkIn.setCheckInCode("CI" + System.currentTimeMillis());
        checkIn.setTitle(elderDto.getName() + "-入住申请");
        checkIn.setElderId(elderId);
        checkIn.setCounselor("");
        checkIn.setCheckInTime(configDto.getCheckInStartTime() != null
                ? parseDateTime(configDto.getCheckInStartTime()) : null);
        // applicat = 家属姓名
        checkIn.setApplicat(primaryMemberName != null ? primaryMemberName : "");
        checkIn.setApplicatId(primaryMemberId != null ? primaryMemberId : 0L);
        checkIn.setDeptNo("0");
        checkIn.setFlowStatus(0);
        checkIn.setStatus(0);
        checkInMapper.insertCheckIn(checkIn);

        // 5. 插入入住配置
        checkInMapper.insertCheckInConfig(
                elderId,
                configDto.getCode(),
                configDto.getNursingLevelId(),
                configDto.getCheckInStartTime(), configDto.getCheckInEndTime(),
                configDto.getCostStartTime(), configDto.getCostEndTime(),
                configDto.getDepositAmount(), configDto.getNursingCost(),
                configDto.getBedCost(), configDto.getOtherCost(),
                configDto.getMedicalInsurancePayment(), configDto.getGovernmentSubsidy()
        );

        // 5.1 更新床位状态为"已入住"
        if (configDto.getBedId() != null) {
            familyMapper.updateBedStatus(configDto.getBedId(), 1);
        }

        // 6. 插入合同
        if (contractDto != null) {
            String contractNo = "CT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    + "-" + String.format("%03d", new Random().nextInt(1000));
            Long createBy = userId != null ? Long.valueOf(userId) : 1L;
            checkInMapper.insertContract(
                    elderId, elderDto.getName(),
                    contractDto.getName(), contractNo,
                    contractDto.getSignDate(),
                    configDto.getCheckInStartTime(), configDto.getCheckInEndTime(),
                    contractDto.getMemberName(), contractDto.getMemberPhone(),
                    contractDto.getPdfUrl(),
                    configDto.getNursingLevelName(),
                    checkIn.getCheckInCode(),
                    0,
                    createBy
            );
        }

        // 7. 创建首月账单
        Bill bill = new Bill();
        bill.setBillNo("BL-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%03d", new Random().nextInt(1000)));
        bill.setBillType(1); // 月度账单
        bill.setBillMonth(parseBillMonth(configDto.getCostStartTime()));
        bill.setElderId(elderId);

        // 计算本期应付 = 护理费 + 床位费 + 其他费用 - 医保支付 - 政府补贴
        BigDecimal currentCost = safeDecimal(configDto.getNursingCost())
                .add(safeDecimal(configDto.getBedCost()))
                .add(safeDecimal(configDto.getOtherCost()))
                .subtract(safeDecimal(configDto.getMedicalInsurancePayment()))
                .subtract(safeDecimal(configDto.getGovernmentSubsidy()));
        bill.setCurrentCost(currentCost);

        // 账单金额 = 本期应付 + 押金
        BigDecimal deposit = safeDecimal(configDto.getDepositAmount());
        bill.setBillAmount(currentCost.add(deposit));
        bill.setPayableAmount(currentCost.add(deposit));
        bill.setDepositAmount(deposit);
        bill.setPrepaidAmount(BigDecimal.ZERO);

        bill.setBillStartTime(parseDateTime(configDto.getCostStartTime()));
        bill.setBillEndTime(parseDateTime(configDto.getCostEndTime()));
        bill.setTotalDays(calcTotalDays(configDto.getCostStartTime(), configDto.getCostEndTime()));
        bill.setPaymentDeadline(parseDateTime(configDto.getCostStartTime())); // 支付截止时间为费用开始日
        bill.setTransactionStatus(0); // 未支付
        bill.setLname(configDto.getNursingLevelName());
        bill.setTypeName(configDto.getFloorName());
        bill.setCreateBy(userId != null ? Long.valueOf(userId) : 1L);
        billMapper.insertBill(bill);

        // 8. 删除Redis缓存
        clearCheckInCache();

        return Result.ok("申请成功");
    }

    /** 入住详情 */
    @Override
    public Result detail(Long id) {
        if (id == null) {
            return Result.fail("入住ID不能为空");
        }

        Map<String, Object> detail = checkInMapper.selectDetailById(id);
        if (detail == null) {
            return Result.fail("入住申请不存在");
        }

        // 组装 checkInElderVo
        Map<String, Object> elderVo = new HashMap<>();
        elderVo.put("id", detail.remove("elderId"));
        elderVo.put("name", detail.remove("elderName"));
        elderVo.put("idCardNo", detail.remove("elderIdCardNo"));
        elderVo.put("phone", detail.remove("elderPhone"));
        elderVo.put("sex", detail.remove("elderSex"));
        elderVo.put("age", detail.remove("elderAge"));
        elderVo.put("image", detail.remove("elderImage"));
        elderVo.put("bedNumber", detail.remove("bedNumber"));
        elderVo.put("bedId", detail.remove("bedId"));
        detail.put("checkInElderVo", elderVo);

        // 组装 checkInConfigVo
        Map<String, Object> configVo = new HashMap<>();
        configVo.put("bedId", detail.remove("bedId"));
        configVo.put("bedNo", detail.remove("configBedNo"));
        configVo.put("checkInStartTime", detail.remove("checkInStartTime"));
        configVo.put("checkInEndTime", detail.remove("checkInEndTime"));
        configVo.put("costStartTime", detail.remove("costStartTime"));
        configVo.put("costEndTime", detail.remove("costEndTime"));
        configVo.put("depositAmount", detail.remove("depositAmount"));
        configVo.put("nursingCost", detail.remove("nursingCost"));
        configVo.put("bedCost", detail.remove("bedCost"));
        configVo.put("otherCost", detail.remove("otherCost"));
        configVo.put("medicalInsurancePayment", detail.remove("medicalInsurancePayment"));
        configVo.put("governmentSubsidy", detail.remove("governmentSubsidy"));
        configVo.put("nursingLevelId", detail.remove("nursingLevelId"));
        configVo.put("nursingLevelName", detail.remove("nursingLevelName"));
        detail.put("checkInConfigVo", configVo);

        // 组装 checkInContractVo
        Map<String, Object> contractVo = new HashMap<>();
        contractVo.put("name", detail.remove("contractName"));
        contractVo.put("signDate", detail.remove("signDate"));
        contractVo.put("memberName", detail.remove("memberName"));
        contractVo.put("memberPhone", detail.remove("memberPhone"));
        contractVo.put("pdfUrl", detail.remove("pdfUrl"));
        contractVo.put("contractNo", detail.remove("contractNo"));
        detail.put("checkInContractVo", contractVo);

        // 修复 bedNo → bedNumber（前端 ApplyConfigBase 取值 bedNumber）
        if (configVo.containsKey("bedNo")) {
            configVo.put("bedNumber", configVo.remove("bedNo"));
        }

        // 查询家属信息
        List<Map<String, Object>> familyList = familyMapper.selectFamilyByElderId(
                Long.valueOf(elderVo.get("id").toString()));
        detail.put("elderFamilyVoList", familyList != null ? familyList : new ArrayList<>());

        return Result.ok(detail);
    }

    /** 清除入住申请缓存 */
    private void clearCheckInCache() {
        Set<String> keys = redisTemplate.keys(CHECK_IN_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // 退住列表缓存key
    private static final String RETREAT_LIST_KEY = RedisKeyProperties.RETREAT_LIST;

    // ==================== 退住 V2 ====================

    /** 分页查询退住申请列表 */
    @Override
    public Result retreatPageQuery(Integer pageNum, Integer pageSize,
                                   String elderName, String elderIdCardNo,
                                   String retreatStartTime, String retreatEndTime) {
        boolean noFilter = (elderName == null || elderName.isEmpty())
                && (elderIdCardNo == null || elderIdCardNo.isEmpty())
                && (retreatStartTime == null || retreatStartTime.isEmpty())
                && (retreatEndTime == null || retreatEndTime.isEmpty());

        if (noFilter) {
            String cacheKey = RETREAT_LIST_KEY + pageNum + ":" + pageSize;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return Result.ok(cached);
            }
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = checkInMapper.selectRetreatPageQuery(
                elderName, elderIdCardNo, retreatStartTime, retreatEndTime);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("pages", pageInfo.getPages());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);

        if (noFilter) {
            String cacheKey = RETREAT_LIST_KEY + pageNum + ":" + pageSize;
            redisTemplate.opsForValue().set(cacheKey, data, 5, java.util.concurrent.TimeUnit.MINUTES);
        }

        return Result.ok(data);
    }

    /** 退住详情 */
    @Override
    public Result retreatDetail(Long id) {
        if (id == null) {
            return Result.fail("退住ID不能为空");
        }

        Map<String, Object> detail = checkInMapper.selectRetreatDetailById(id);
        if (detail == null) {
            return Result.fail("退住申请不存在");
        }

        // 组装 retreatElderVo
        Map<String, Object> elderVo = new HashMap<>();
        elderVo.put("name", detail.remove("name"));
        elderVo.put("checkOutTime", detail.remove("checkOutTime"));
        elderVo.put("reason", detail.remove("reason"));
        elderVo.put("idCardNo", detail.remove("idCardNo"));
        elderVo.put("checkInStartTime", detail.remove("checkInStartTime"));
        elderVo.put("checkInEndTime", detail.remove("checkInEndTime"));
        elderVo.put("costStartTime", detail.remove("costStartTime"));
        elderVo.put("costEndTime", detail.remove("costEndTime"));
        elderVo.put("nursingLevelName", detail.remove("nursingLevelName"));
        elderVo.put("bedNumber", detail.remove("bedNumber"));
        elderVo.put("nursingName", detail.remove("nursingName"));
        elderVo.put("phone", detail.remove("phone"));
        elderVo.put("applicat", detail.remove("applicat"));
        detail.put("retreatElderVo", elderVo);

        // 组装 refundVoucherVo
        Map<String, Object> voucherVo = new HashMap<>();
        voucherVo.put("refundVoucherUrl", detail.remove("refundVoucherUrl"));
        voucherVo.put("createTime", detail.remove("refundCreateTime"));
        voucherVo.put("tradingChannel", detail.remove("tradingChannel"));
        voucherVo.put("remark", detail.remove("refundRemark"));
        detail.put("refundVoucherVo", voucherVo);

        return Result.ok(detail);
    }

    /** 提交退住申请 */
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Result retreatApply(RetreatApplyDTO dto) {
        RetreatElderDTO elderDto = dto.getRetreatElderDto();
        RefundVoucherDTO voucherDto = dto.getRefundVoucherDto();

        if (elderDto == null || elderDto.getId() == null) {
            return Result.fail("老人信息不能为空");
        }
        if (elderDto.getCheckOutTime() == null || elderDto.getCheckOutTime().isBlank()) {
            return Result.fail("退住日期不能为空");
        }

        Long elderId = elderDto.getId();
        String userId = UserHolder.getId();

        // 1. 插入退住记录
        Map<String, Object> retreatParams = new HashMap<>();
        String retreatCode = "RT" + System.currentTimeMillis();
        retreatParams.put("retreatCode", retreatCode);
        retreatParams.put("title", elderDto.getName() + "-退住申请");
        retreatParams.put("elderId", elderId);
        retreatParams.put("name", elderDto.getName());
        retreatParams.put("idCardNo", elderDto.getIdCardNo());
        retreatParams.put("phone", elderDto.getPhone());
        retreatParams.put("checkInStartTime", elderDto.getCheckInStartTime());
        retreatParams.put("checkInEndTime", elderDto.getCheckInEndTime());
        retreatParams.put("nursingLevelName", elderDto.getNursingLevelName());
        retreatParams.put("bedNo", elderDto.getBedNo() != null ? elderDto.getBedNo() : elderDto.getBedNumber());
        retreatParams.put("nursingName", elderDto.getNursingName());
        retreatParams.put("checkOutTime", elderDto.getCheckOutTime());
        retreatParams.put("reason", elderDto.getReason());
        retreatParams.put("applicat", "");
        retreatParams.put("applicatId", userId != null ? Long.valueOf(userId) : 0L);
        retreatParams.put("deptNo", "0");
        retreatParams.put("flowStatus", 0);
        retreatParams.put("status", 1);
        checkInMapper.insertRetreat(retreatParams);

        // 获取插入后的退住ID（MyBatis 返回 BigInteger，需转换）
        Long retreatId = ((Number) retreatParams.get("id")).longValue();

        // 2. 插入退住账单
        if (dto.getBillJson() != null || voucherDto != null) {
            checkInMapper.insertRetreatBill(
                    retreatId,
                    dto.getBillJson(),
                    voucherDto != null ? voucherDto.getRefundVoucherUrl() : null,
                    voucherDto != null ? voucherDto.getTradingChannel() : null,
                    elderId,
                    0
            );
        }

        // 3. 更新合同解除信息
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isBlank()) {
            String submitter = elderDto.getName();
            checkInMapper.updateContractRelease(
                    elderId,
                    dto.getReleaseDate(),
                    dto.getReleasePdfUrl(),
                    submitter
            );
        }

        // 4. 更新老人状态为退住中
        familyMapper.updateElderStatus(elderId, 3);

        // 5. 更新床位状态为可用
        if (elderDto.getBedId() != null) {
            familyMapper.updateBedStatus(elderDto.getBedId(), 0);
        }

        // 6. 清除Redis缓存
        clearRetreatCache();

        return Result.ok("退住申请已提交");
    }

    /** 清除退住列表缓存 */
    private void clearRetreatCache() {
        Set<String> keys = redisTemplate.keys(RETREAT_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            return LocalDateTime.parse(str.replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }

    /** 安全转换 BigDecimal，null 视为 0 */
    private BigDecimal safeDecimal(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    /** 从日期字符串提取账单月份 "yyyy-MM" */
    private String parseBillMonth(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            LocalDateTime dt = parseDateTime(dateStr);
            return dt != null ? dt.format(DateTimeFormatter.ofPattern("yyyy-MM")) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** 计算两个日期字符串之间的天数 */
    private Integer calcTotalDays(String start, String end) {
        if (start == null || start.isEmpty() || end == null || end.isEmpty()) return 0;
        try {
            LocalDateTime s = parseDateTime(start);
            LocalDateTime e = parseDateTime(end);
            if (s == null || e == null) return 0;
            return (int) java.time.temporal.ChronoUnit.DAYS.between(s.toLocalDate(), e.toLocalDate());
        } catch (Exception ex) {
            return 0;
        }
    }
}
