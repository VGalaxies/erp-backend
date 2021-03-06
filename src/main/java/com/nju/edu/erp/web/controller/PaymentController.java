package com.nju.edu.erp.web.controller;

import com.nju.edu.erp.auth.Authorized;
import com.nju.edu.erp.enums.Role;
import com.nju.edu.erp.enums.sheetState.PaymentSheetState;
import com.nju.edu.erp.model.vo.UserVO;
import com.nju.edu.erp.model.vo.finance.PaymentSheetVO;
import com.nju.edu.erp.service.PaymentService;
import com.nju.edu.erp.utils.IdDateUtil;
import com.nju.edu.erp.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/payment")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF})
    @PostMapping(value = "/sheet-make")
    public Response makePaymentSheet(UserVO userVO, @RequestBody PaymentSheetVO paymentSheetVO) {
        paymentService.makePaymentSheet(userVO, paymentSheetVO);
        return Response.buildSuccess();
    }

    @GetMapping(value = "/approval")
    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF})
    public Response firstApproval(@RequestParam("paymentSheetId") String paymentSheetId,
                                  @RequestParam("state") PaymentSheetState state) {
        if (state.equals(PaymentSheetState.FAILURE) || state.equals(PaymentSheetState.SUCCESS)) {
            paymentService.approval(paymentSheetId, state);
            return Response.buildSuccess();
        } else {
            return Response.buildFailed("000000", "操作失败");
        }
    }

    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF})
    @GetMapping(value = "/sheet-show")
    public Response showSheetByState(@RequestParam(value = "state", required = false) PaymentSheetState state) {
        return Response.buildSuccess(paymentService.getPaymentSheetByState(state));
    }

    @GetMapping(value = "/sheet-show-filter")
    public Response showSheetFilter(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "operator", required = false) String operator,
            @RequestParam(value = "customerId", required = false) Integer customerId) {
        return Response.buildSuccess(paymentService.getPaymentSheetByState(null).stream().filter(
                paymentSheetVO -> {
                    Date date = IdDateUtil.parseDateFromSheetId(paymentSheetVO.getId(), "FKD");
                    return (((from == null && to == null) || (date.after(IdDateUtil.parseDateFromStr(from)) && date.before(IdDateUtil.parseDateFromStr(to))))
                            && (operator == null || paymentSheetVO.getOperator().equals(operator))
                            && (customerId == null || paymentSheetVO.getSupplier().equals(customerId))
                    );
                }
        ).collect(Collectors.toList()));
    }

    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF})
    @GetMapping(value = "/find-sheet")
    public Response findBySheetId(@RequestParam(value = "id") String id) {
        return Response.buildSuccess(paymentService.getPaymentSheetById(id));
    }
}
