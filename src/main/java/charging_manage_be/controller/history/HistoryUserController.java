package charging_manage_be.controller.history;

import charging_manage_be.model.dto.history.HistoryPaymentDTO;
import charging_manage_be.model.dto.history.HistoryPostDTO;
import charging_manage_be.model.dto.history.HistoryResponseDTO;
import charging_manage_be.model.dto.history.HistoryStationDTO;
import charging_manage_be.model.entity.charging.ChargingSessionEntity;
import charging_manage_be.model.entity.payments.PaymentEntity;
import charging_manage_be.services.charging_session.ChargingSessionService;
import charging_manage_be.services.payments.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryUserController {

    @Autowired
    private ChargingSessionService chargingSessionService;
    @Autowired
    private PaymentService paymentService;
    @GetMapping("/{userId}")
    public ResponseEntity<List<HistoryResponseDTO>> getHistoryByUserId(@PathVariable String userId)
    {
        List<HistoryResponseDTO> response = new ArrayList<>();
        List<ChargingSessionEntity> listSession =  chargingSessionService.getAllSessionsByUserId(userId);
        for(ChargingSessionEntity session : listSession)
        {
            HistoryStationDTO station  = new HistoryStationDTO();
            station.setId(session.getStation().getIdChargingStation());
            station.setName(session.getStation().getNameChargingStation());
            station.setAddress(session.getStation().getAddress());

            HistoryPostDTO post = new HistoryPostDTO();
            post.setId(session.getChargingPost().getIdChargingPost());
            post.setMaxPower(session.getChargingPost().getMaxPower());

            PaymentEntity paymentEntity  = paymentService.getPaymentBySessionId(session.getChargingSessionId());
            HistoryPaymentDTO payment = new HistoryPaymentDTO();
            if(paymentEntity != null)
            {
                payment.setId(paymentEntity.getPaymentId());
                payment.setPaid(paymentEntity.isPaid());
                payment.setMethodId(paymentEntity.getPaymentMethod().getIdPaymentMethod());
                payment.setMethodName(paymentEntity.getPaymentMethod().getNamePaymentMethod());
                payment.setPaidAt(paymentEntity.getPaidAt());
            }

            HistoryResponseDTO term = new  HistoryResponseDTO();
            term.setSessionId(session.getChargingSessionId());
            term.setStartTime(session.getStartTime());
            term.setEndTime(session.getEndTime());
            term.setKWh(session.getKWh());
            term.setTotalAmount(session.getTotalAmount());
            term.setDone(session.isDone());
            term.setPayment(payment);
            term.setStation(station);
            term.setPost(post);
            response.add(term);

        }
        return  ResponseEntity.ok(response);
    }
}
