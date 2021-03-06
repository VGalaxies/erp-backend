package com.nju.edu.erp.service.Impl;

import com.nju.edu.erp.dao.CustomerDao;
import com.nju.edu.erp.enums.CustomerType;
import com.nju.edu.erp.exception.MyServiceException;
import com.nju.edu.erp.model.po.CustomerPO;
import com.nju.edu.erp.model.vo.CustomerVO;
import com.nju.edu.erp.service.CustomerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDao customerDao;

    @Autowired
    public CustomerServiceImpl(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    /**
     * 根据id更新客户信息
     *
     * @param customerPO 客户信息
     */
    @Override
    public void updateCustomer(CustomerPO customerPO) {
        customerDao.updateOne(customerPO);
    }

    /**
     * 根据type查找对应类型的客户
     *
     * @param type 客户类型
     * @return 客户列表
     */
    @Override
    public List<CustomerPO> getCustomersByType(CustomerType type) {

        return customerDao.findAllByType(type);
    }

    @Override
    public CustomerPO findCustomerById(Integer supplier) {
        return customerDao.findOneById(supplier);
    }

    @Override
    public void addCustomer(CustomerVO customerVO) {
        CustomerPO customerPO = customerDao.findOneById(customerVO.getId());
        if (customerPO != null) {
            throw new MyServiceException("A0002", "客户已存在");
        }
        CustomerPO customerSave = new CustomerPO();
        BeanUtils.copyProperties(customerVO, customerSave);
        customerDao.createCustomer(customerSave);
    }

    @Override
    public void deleteCustomerById(Integer id) {
        CustomerPO customerPO = customerDao.findOneById(id);
        if (customerPO != null) {
            customerDao.deleteCustomerById(id);
        }
    }

    @Override
    public void updateCustomer(CustomerVO customerVO) {
        CustomerPO customerPO = customerDao.findOneById(customerVO.getId());
        if (customerPO != null) {
            CustomerPO updatedCustomerPO = new CustomerPO();
            BeanUtils.copyProperties(customerVO, updatedCustomerPO);
            customerDao.updateOne(updatedCustomerPO);
        }
    }
}
