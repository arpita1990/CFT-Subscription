package com.cft.subscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "Utils_Config")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionExpiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int utilsConfigId;

    @Column(name="configType")
    private  String config_type;

    @Column(name="configValue")
    private  String config_value;
}
