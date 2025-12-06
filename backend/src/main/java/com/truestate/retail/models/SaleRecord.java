package com.truestate.retail.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sale_records")
public class SaleRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private LocalDate date;

    private String customerId;
    private String customerName;
    private String phoneNumber;
    private String gender;
    private Integer age;
    private String customerRegion;
    private String customerType;

    private String productId;
    private String productName;
    private String brand;
    private String productCategory;

    @Column(length = 512)
    private String tags; // comma separated tags

    private Integer quantity;
    private Double pricePerUnit;
    private Double discountPercentage;
    private Double totalAmount;
    private Double finalAmount;

    private String paymentMethod;
    private String orderStatus;
    private String deliveryType;
    private String storeId;
    private String storeLocation;
    private String salespersonId;
    private String employeeName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getCustomerRegion() { return customerRegion; }
    public void setCustomerRegion(String customerRegion) { this.customerRegion = customerRegion; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(Double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreLocation() { return storeLocation; }
    public void setStoreLocation(String storeLocation) { this.storeLocation = storeLocation; }

    public String getSalespersonId() { return salespersonId; }
    public void setSalespersonId(String salespersonId) { this.salespersonId = salespersonId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
