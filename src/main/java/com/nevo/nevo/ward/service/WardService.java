package com.nevo.nevo.ward.service;

import com.nevo.nevo.global.exception.CustomException;
import com.nevo.nevo.ward.dto.WardRequest;
import com.nevo.nevo.ward.dto.WardResponse;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.exception.code.WardErrorCode;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WardService {

    private final WardRepository wardRepository;

    @Transactional(readOnly = true)
    public WardResponse.PhysicalInfo getMyPhysicalInfo(Long wardId) {
        Ward ward = findWard(wardId);

        return WardResponse.PhysicalInfo.from(ward);
    }

    @Transactional
    public WardResponse.PhysicalInfo updateMyPhysicalInfo(
            Long wardId,
            WardRequest.UpsertPhysicalInfo request
    ) {
        Ward ward = findWard(wardId);

        ward.updatePhysicalInfo(
                request.height(),
                request.weight(),
                request.birthDate(),
                request.gender()
        );

        return WardResponse.PhysicalInfo.from(ward);
    }

    @Transactional
    private Ward findWard(Long wardId) {
        return wardRepository.findById(wardId)
                .orElseThrow(() -> new CustomException(WardErrorCode.WARD_NOT_FOUND));
    }
}
