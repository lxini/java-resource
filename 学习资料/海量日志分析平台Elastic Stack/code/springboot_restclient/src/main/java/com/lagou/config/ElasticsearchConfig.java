package com.lagou.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
    @Value("${lagouelasticsearch.elasticsearch.hostlist}")
    private  String  hostlist;
    @Bean
    public RestHighLevelClient  restHighLevelClient(){
        // 解析hostlist 信息
        String[]  split = hostlist.split(",");
        // 创建HttpHost数组  封装es的主机和端口
        HttpHost[]  httpHosts  = new  HttpHost[split.length];
        for (int i=0;i<split.length;i++){
            String  iterm = split[i];
            httpHosts[i] = new HttpHost(iterm.split(":")[0],Integer.parseInt(iterm.split(":")[1]),"http");
        }
        return   new RestHighLevelClient(RestClient.builder(httpHosts));
    }
}
