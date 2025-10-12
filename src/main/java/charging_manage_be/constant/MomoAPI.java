package charging_manage_be.constant;

    import charging_manage_be.model.dto.momo_payment.CreateMomoRequestDTO;
    import charging_manage_be.model.dto.momo_payment.CreateMomoResponseDTO;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.http.MediaType;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;

    @FeignClient(name = "momo-api", url = "${momo.end-point}")
    public interface MomoAPI {
        @PostMapping(value = "/create",
                     consumes = MediaType.APPLICATION_JSON_VALUE,
                     produces = MediaType.APPLICATION_JSON_VALUE)
        CreateMomoResponseDTO createPayment(@RequestBody CreateMomoRequestDTO request);
    }