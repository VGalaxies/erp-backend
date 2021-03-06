package com.nju.edu.erp.web.controller;

import com.nju.edu.erp.auth.Authorized;
import com.nju.edu.erp.enums.Role;
import com.nju.edu.erp.enums.sheetState.SalarySheetState;
import com.nju.edu.erp.exception.MyServiceException;
import com.nju.edu.erp.model.vo.finance.SalarySheetVO;
import com.nju.edu.erp.service.SalaryService;
import com.nju.edu.erp.utils.IdDateUtil;
import com.nju.edu.erp.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/salary")
public class SalaryController {
    private final SalaryService salaryService;

    @Autowired
    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF, Role.HR})
    @PostMapping(value = "/sheet-make")
    public Response makeSalarySheet(@RequestBody SalarySheetVO salarySheetVO) {
        try {
            salaryService.makeSalarySheet(salarySheetVO);
            return Response.buildSuccess();
        } catch (MyServiceException e) {
            return Response.buildFailed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Response.buildFailed("114514", "未知错误");
        }
    }

    @GetMapping(value = "/approval")
    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF, Role.HR})
    public Response firstApproval(@RequestParam("salarySheetId") String salarySheetId,
                                  @RequestParam("state") SalarySheetState state) {
        if (state.equals(SalarySheetState.FAILURE) || state.equals(SalarySheetState.SUCCESS)) {
            salaryService.approval(salarySheetId, state);
            return Response.buildSuccess();
        } else {
            return Response.buildFailed("000000", "操作失败");
        }
    }

    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF, Role.HR})
    @GetMapping(value = "/sheet-show")
    public Response showSheetByState(@RequestParam(value = "state", required = false) SalarySheetState state) {
        return Response.buildSuccess(salaryService.getSalarySheetByState(state));
    }

    @GetMapping(value = "/sheet-show-filter")
    public Response showSheetFilter(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to) {
        return Response.buildSuccess(salaryService.getSalarySheetByState(null).stream().filter(
                salarySheetVO -> {
                    Date date = IdDateUtil.parseDateFromSheetId(salarySheetVO.getId(), "GZD");
                    return ((from == null && to == null) || (date.after(IdDateUtil.parseDateFromStr(from)) && date.before(IdDateUtil.parseDateFromStr(to))));
                }
        ).collect(Collectors.toList()));
    }

    @Authorized(roles = {Role.GM, Role.ADMIN, Role.FINANCIAL_STAFF, Role.HR})
    @GetMapping(value = "/find-sheet")
    public Response findBySheetId(@RequestParam(value = "id") String id) {
        return Response.buildSuccess(salaryService.getSalarySheetById(id));
    }
}
