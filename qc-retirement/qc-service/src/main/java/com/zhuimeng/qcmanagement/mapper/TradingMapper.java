package com.zhuimeng.qcmanagement.mapper;

import com.zhuimeng.entity.Trading;
import com.zhuimeng.qcmanagement.annotation.AutoCreate;
import com.zhuimeng.qcmanagement.annotation.AutoUpdata;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradingMapper {

    /** 插入交易记录
    * */
    @AutoCreate
    void insert(Trading trading);

    /** 根据交易单号查询
    * */
    Trading selectByTradingOrderNo(Long tradingOrderNo);

    /** 更新交易记录 */
    @AutoUpdata
    void updateTrading(Trading trading);
}
