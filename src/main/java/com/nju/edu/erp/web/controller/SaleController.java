package com.nju.edu.erp.web.controller;

import com.nju.edu.erp.auth.Authorized;
import com.nju.edu.erp.enums.Role;
import com.nju.edu.erp.enums.sheetState.SaleSheetState;
import com.nju.edu.erp.exception.MyServiceException;
import com.nju.edu.erp.model.po.CustomerPurchaseAmountPO;
import com.nju.edu.erp.model.vo.UserVO;
import com.nju.edu.erp.model.vo.sale.SaleSheetVO;
import com.nju.edu.erp.service.SaleService;
import com.nju.edu.erp.service.strategy.PromotionStrategy;
import com.nju.edu.erp.utils.IdDateUtil;
import com.nju.edu.erp.web.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/sale")
public class SaleController {

    private final SaleService saleService;
    private final PromotionStrategy promotionStrategy;

    @Autowired
    public SaleController(SaleService saleService, PromotionStrategy promotionStrategy) {
        this.saleService = saleService;
        this.promotionStrategy = promotionStrategy;
    }

    @GetMapping("/get-total-promotion")
    @Authorized(roles = {Role.SALE_STAFF, Role.SALE_MANAGER, Role.GM, Role.ADMIN})
    public Response getTotalPromotion(@RequestParam("total") String total) {
        try {
            return Response.buildSuccess(promotionStrategy.getOnePromotionByType("total", total));
        } catch (MyServiceException e) {
            return Response.buildFailed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Response.buildFailed("111111", "Unknown Exception");
        }
    }

    @GetMapping("/get-customer-promotion")
    @Authorized(roles = {Role.SALE_STAFF, Role.SALE_MANAGER, Role.GM, Role.ADMIN})
    public Response getCustomerPromotion(@RequestParam("level") String level) {
        try {
            return Response.buildSuccess(promotionStrategy.getOnePromotionByType("customer", level));
        } catch (MyServiceException e) {
            return Response.buildFailed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Response.buildFailed("111111", "Unknown Exception");
        }
    }

    @GetMapping("/get-combine-promotion")
    @Authorized(roles = {Role.SALE_STAFF, Role.SALE_MANAGER, Role.GM, Role.ADMIN})
    public Response getCombinePromotion(@RequestParam("pids") String pids) {
        try {
            return Response.buildSuccess(promotionStrategy.getOnePromotionByType("combine", pids));
        } catch (MyServiceException e) {
            return Response.buildFailed(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return Response.buildFailed("111111", "Unknown Exception");
        }
    }

    /**
     * ???????????????????????????
     */
    @Authorized(roles = {Role.SALE_STAFF, Role.SALE_MANAGER, Role.GM, Role.ADMIN})
    @PostMapping(value = "/sheet-make")
    public Response makeSaleOrder(UserVO userVO, @RequestBody SaleSheetVO saleSheetVO) {
        saleService.makeSaleSheet(userVO, saleSheetVO);
        return Response.buildSuccess();
    }

    /**
     * ???????????????????????????
     */
    @GetMapping(value = "/sheet-show")
    public Response showSheetByState(@RequestParam(value = "state", required = false) SaleSheetState state) {
        return Response.buildSuccess(saleService.getSaleSheetByState(state));
    }

    @GetMapping(value = "/sheet-show-filter")
    public Response showSheetFilter(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "salesman", required = false) String salesman,
            @RequestParam(value = "customerId", required = false) Integer customerId) {
        return Response.buildSuccess(saleService.getSaleSheetByState(null).stream().filter(
                saleSheetVO -> {
                    Date date = IdDateUtil.parseDateFromSheetId(saleSheetVO.getId(), "XSD");
                    return (((from == null && to == null) || (date.after(IdDateUtil.parseDateFromStr(from)) && date.before(IdDateUtil.parseDateFromStr(to))))
                            && (salesman == null || saleSheetVO.getSalesman().equals(salesman))
                            && (customerId == null || saleSheetVO.getSupplier().equals(customerId))
                    );
                }
        ).collect(Collectors.toList()));
    }

    /**
     * ??????????????????
     *
     * @param saleSheetId ?????????id
     * @param state       ??????????????????("????????????"/"???????????????")
     */
    @GetMapping(value = "/first-approval")
    @Authorized(roles = {Role.SALE_MANAGER, Role.ADMIN})
    public Response firstApproval(@RequestParam("saleSheetId") String saleSheetId,
                                  @RequestParam("state") SaleSheetState state) {
        if (state.equals(SaleSheetState.FAILURE) || state.equals(SaleSheetState.PENDING_LEVEL_2)) {
            saleService.approval(saleSheetId, state);
            return Response.buildSuccess();
        } else {
            return Response.buildFailed("000000", "????????????"); // code???????????????????????????
        }
    }

    /**
     * ???????????????
     *
     * @param saleSheetId ?????????id
     * @param state       ??????????????????("????????????"/"????????????")
     */
    @Authorized(roles = {Role.GM, Role.ADMIN})
    @GetMapping(value = "/second-approval")
    public Response secondApproval(@RequestParam("saleSheetId") String saleSheetId,
                                   @RequestParam("state") SaleSheetState state) {
        if (state.equals(SaleSheetState.FAILURE) || state.equals(SaleSheetState.SUCCESS)) {
            saleService.approval(saleSheetId, state);
            return Response.buildSuccess();
        } else {
            return Response.buildFailed("000000", "????????????"); // code???????????????????????????
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????(?????????????????????,??????????????????????????????,??????????????????????????????,???????????????)
     *
     * @param salesman     ?????????????????????
     * @param beginDateStr ????????????????????? ????????????yyyy-MM-dd HH:mm:ss????????????2022-05-12 11:38:30???
     * @param endDateStr   ????????????????????? ????????????yyyy-MM-dd HH:mm:ss????????????2022-05-12 11:38:30???
     * @return
     */
    @GetMapping("/maxAmountCustomer")
    @Authorized(roles = {Role.SALE_MANAGER, Role.GM, Role.ADMIN})
    public Response getMaxAmountCustomerOfSalesmanByTime(@RequestParam String salesman, @RequestParam String beginDateStr, @RequestParam String endDateStr) {
        CustomerPurchaseAmountPO ans = saleService.getMaxAmountCustomerOfSalesmanByTime(salesman, beginDateStr, endDateStr);
        return Response.buildSuccess(ans);
    }

    /**
     * ???????????????Id?????????????????????
     *
     * @param id ?????????Id
     * @return ?????????????????????
     */
    @GetMapping(value = "/find-sheet")
    public Response findBySheetId(@RequestParam(value = "id") String id) {
        return Response.buildSuccess(saleService.getSaleSheetById(id));
    }

}
