package com.example.shardingdemo.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Author: WangYuanrong
 * @Date: 2021/4/21 10:52
 */
@Configuration
public class DataSourceConfig {

    @Autowired
    private Ds0Properties ds0Properties;

    @Autowired
    private Ds1Properties ds1Properties;

    private DataSource druidDataSource(DataSourceProperties dataSourceProperties) throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
        druidDataSource.setUrl(dataSourceProperties.getUrl());
        druidDataSource.setUsername(dataSourceProperties.getUsername());
        druidDataSource.setPassword(dataSourceProperties.getPassword());
        druidDataSource.setInitialSize(dataSourceProperties.getInitialSize());
        druidDataSource.setMinIdle(dataSourceProperties.getMinIdle());
        druidDataSource.setMaxActive(dataSourceProperties.getMaxActive());
        druidDataSource.setMaxWait(dataSourceProperties.getMaxWait());
        druidDataSource.setFilters("stat");
        druidDataSource.setKeepAlive(true);
        druidDataSource.setRemoveAbandoned(true);
        druidDataSource.setRemoveAbandonedTimeout(180);
        druidDataSource.setUseGlobalDataSourceStat(true);
        return druidDataSource;
    }


    @Bean
    public DataSource dataSource() throws SQLException {

        // ?????????????????????
        Map<String, DataSource> dataSourceMap = new HashMap<>(2);
        // ????????? 1 ????????????
        DataSource ds0 = druidDataSource(ds0Properties);
        dataSourceMap.put("ds0", ds0);

        // ????????? 2 ????????????
        DataSource ds1 = druidDataSource(ds1Properties);
        dataSourceMap.put("ds1", ds1);

        // ?????? t_order ?????????
        ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_order_", "ds${0..1}.t_order_${0..1}");
        // ??????????????????
        orderTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "dbShardingAlgorithm"));
        // ??????????????????
        orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("id", "tableShardingAlgorithm"));


        // ?????? t_order_item ?????????
        ShardingTableRuleConfiguration orderItemTableRuleConfig = new ShardingTableRuleConfiguration("t_order_item_", "ds1.t_order_item_${0..1}");
        // ??????????????????
        orderItemTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "orderItemDbShardingAlgorithm"));
        // ??????????????????
        orderItemTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("id", "orderItemTableShardingAlgorithm"));

        // ?????? t_user ?????????
        ShardingTableRuleConfiguration userTableRuleConfig = new ShardingTableRuleConfiguration("t_user", "ds0.t_user");
        // ?????? t_msg ?????????
        ShardingTableRuleConfiguration msgTableRuleConfig = new ShardingTableRuleConfiguration("t_msg", "ds1.t_msg");


        // ??????????????????
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(orderTableRuleConfig);
        shardingRuleConfig.getTables().add(orderItemTableRuleConfig);

        // ??????????????????
        Properties dbShardingAlgorithmrProps = new Properties();
        dbShardingAlgorithmrProps.setProperty("algorithm-expression", "ds${user_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("dbShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", dbShardingAlgorithmrProps));

        // ??????????????????
        Properties tableShardingAlgorithmrProps = new Properties();
        tableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_order_${id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));


        // ??????????????????
        Properties orderItemTableShardingAlgorithmrProps = new Properties();
        orderItemTableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_order_item_${id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("orderItemTableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", orderItemTableShardingAlgorithmrProps));

        // ?????? ShardingSphereDataSource
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
    }


}
