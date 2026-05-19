package com.nevo.nevo.notification.service;

import com.nevo.nevo.notification.entity.AlertType;
import com.nevo.nevo.session.event.StrokeDangerEvent;
import com.nevo.nevo.ward.entity.Ward;
import com.nevo.nevo.ward.entity.WardGuardianLink;
import com.nevo.nevo.ward.repository.WardGuardianLinkRepository;
import com.nevo.nevo.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final FcmService fcmService;
    private final AlertService alertService;
    private final WardGuardianLinkRepository wardGuardianLinkRepository;
    private final WardRepository wardRepository;

    // @Transactional: 새 스레드에서 트랜잭션을 열어 LAZY 로딩과 alert 저장이 가능하게 함
    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStrokeDanger(StrokeDangerEvent event) {
        Ward ward = wardRepository.findById(event.wardId()).orElse(null);
        if (ward == null) return;

        String wardName = ward.getUser().getName();
        String message = wardName + "님에게 이상 보행이 감지되었습니다.";

        alertService.save(ward, AlertType.STROKE_DANGER, event.sessionId(), message);

        List<WardGuardianLink> links = wardGuardianLinkRepository.findAllByWard_Id(event.wardId());
        for (WardGuardianLink link : links) {
            String fcmToken = link.getGuardian().getFcmToken();
            if (fcmToken == null) continue;

            fcmService.send(
                    fcmToken,
                    "위험 감지",
                    message,
                    Map.of(
                            "type", "STROKE_DANGER",
                            "wardId", String.valueOf(event.wardId()),
                            "sessionId", String.valueOf(event.sessionId())
                    )
            );
        }

        log.info("위험 감지 알림 처리 완료 — wardId: {}, 보호자 수: {}", event.wardId(), links.size());
    }
}