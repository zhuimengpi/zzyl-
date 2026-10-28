package com.zhuimeng.qcmanagement.service;

import com.zhuimeng.Result.Result;
import com.zhuimeng.dto.BedDTO;

public interface BedService {
    Result readRoom(Long id);

    Result read(Long id);

    Result create(BedDTO bedDTO);

    Result update(BedDTO bedDTO);

    Result delete(Long id);
}
