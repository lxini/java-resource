package com.lagou;

import org.apache.http.util.EntityUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;

import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {
    @Autowired
    RestHighLevelClient  client;
    // 创建索引库
    /*
    *
    PUT /elasticsearch_test
    {
      "settings": {},
      "mappings": {
        "properties": {
          "description": {
            "type": "text",
            "analyzer": "ik_max_word"
          },
          "name": {
            "type": "keyword"
          },
          "pic": {
            "type": "text",
            "index": false
          },
          "studymodel": {
            "type": "keyword"
          }
        }
      }
    }
    * */
    @Test
   public   void   testCreateIndex() throws IOException {
       // 创建一个索引创建请求对象
        CreateIndexRequest  createIndexRequest  = new CreateIndexRequest("elasticsearch_test");
        //设置映射
       /* XContentBuilder   builder  = XContentFactory.jsonBuilder()
                .startObject()
                .field("properties")
                .startObject()
                .field("description").startObject().field("type","text").field("analyzer","ik_max_word").endObject()
                .field("name").startObject().field("type","keyword").endObject()
                .field("pic").startObject().field("type","text").field("index","false").endObject()
                .field("studymodel").startObject().field("type","keyword").endObject()
                .endObject()
                .endObject();
        createIndexRequest.mapping("doc",builder);

        */

        createIndexRequest.mapping("doc","{\n" +
                "        \"properties\": {\n" +
                "          \"description\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"ik_max_word\"\n" +
                "          },\n" +
                "          \"name\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"pic\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"index\": false\n" +
                "          },\n" +
                "          \"studymodel\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      }", XContentType.JSON);
        // 操作索引的客户端
        IndicesClient  indicesClient  = client.indices();

        CreateIndexResponse  createIndexResponse = indicesClient.create(createIndexRequest, RequestOptions.DEFAULT);
        // 得到响应
        boolean  acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
   }

   @Test
    public   void   testDeleteIndex() throws  IOException{
        // 构建 删除索引库的请求对象
        DeleteIndexRequest   deleteIndexRequest  = new DeleteIndexRequest("elasticsearch_test");
        IndicesClient   indicesClient  = client.indices();

        AcknowledgedResponse  deleteResponse = indicesClient.delete(deleteIndexRequest,RequestOptions.DEFAULT);
        // 得到响应
        boolean   acknowledge = deleteResponse.isAcknowledged();
       System.out.println(acknowledge);
    }


   //添加文档
    /*
    POST /elasticsearch_test/_doc/1
    {
      "name": "spring cloud实战",
      "description": "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。",
      "studymodel":"201001",
      "timestamp": "2020-08-22 20:09:18",
      "price": 5.6
    }
     */
    @Test
    public   void   testAddDoc()throws  IOException{
        // 准备索取请求对象
        //IndexRequest indexRequest  = new IndexRequest("elasticsearch_test","doc");
        IndexRequest indexRequest  = new IndexRequest("elasticsearch_test");

        //indexRequest.id("2");
        // 文档内容  准备json数据
        Map<String,Object>  jsonMap = new HashMap<>();
        jsonMap.put("name","spring cloud实战3");
        jsonMap.put("description","本课程主要从四个章节进行讲解3： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel","3101001");
        jsonMap.put("timestamp","2020-07-22 20:09:18");
        jsonMap.put("price",35.6);
        indexRequest.source(jsonMap);
        // 执行请求
        IndexResponse  indexResponse = client.index(indexRequest,RequestOptions.DEFAULT);
        DocWriteResponse.Result  result = indexResponse.getResult();
        System.out.println(result);
    }

    // 查询文档
    @Test
    public  void  testGetDoc()throws  IOException{
        // 查询请求对象
        GetRequest  getRequest  = new GetRequest("elasticsearch_test","12");
        GetResponse  getResponse  = client.get(getRequest,RequestOptions.DEFAULT);

        // 得到文档内容
        Map<String,Object> sourceMap = getResponse.getSourceAsMap();
        System.out.println(sourceMap);
    }

    //搜索全部记录
    /*
       GET   /elasticsearch_test/_search
        {
          "query":{
             "match_all":{}
          }
        }
    */
    @Test
    public  void   testSearchAll()throws  IOException{
        // 搜索请求对象
        SearchRequest  searchRequest = new SearchRequest("elasticsearch_test");
        searchRequest.searchType(SearchType.QUERY_THEN_FETCH);
        // 搜索源构建对象
        SearchSourceBuilder  searchSourceBuilder = new SearchSourceBuilder();
        // 设置搜索方法
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.fetchSource(new String[]{"name","price","timestamp"},new String[]{});
        // 请求对象设置 搜索源对象
        searchRequest.source(searchSourceBuilder);
        // 使用client  执行搜索
        SearchResponse searchResponse  = client.search(searchRequest,RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits  hits = searchResponse.getHits();
        // 匹配到的总记录数
        TotalHits  totalHits  = hits.getTotalHits();
        System.out.println("查询到的总记录数:"+totalHits.value);
        // 得到的匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit  hit : searchHits){
            String  id = hit.getId();
            // 源文档的内容
            Map<String,Object>  sourceMap = hit.getSourceAsMap();
            String  name  = (String)sourceMap.get("name");
            String  timestamp  = (String)sourceMap.get("timestamp");
            String  description  = (String)sourceMap.get("description");
            Double   price  = (Double)sourceMap.get("price");
            System.out.println(name);
            System.out.println(timestamp);
            System.out.println(description);
            System.out.println(price);
        }
    }

    @Test
    public  void   testTermQuery()throws  IOException{
        // 搜索请求对象
        SearchRequest  searchRequest = new SearchRequest("elasticsearch_test");
        // 搜索源构建对象
        SearchSourceBuilder  searchSourceBuilder = new SearchSourceBuilder();
        // 设置搜索方法
        //searchSourceBuilder.query(QueryBuilders.termQuery("name","spring cloud实战"));
        searchSourceBuilder.query(QueryBuilders.termQuery("description","spring"));
        searchSourceBuilder.fetchSource(new String[]{"name","price","timestamp"},new String[]{});
        // 请求对象设置 搜索源对象
        searchRequest.source(searchSourceBuilder);
        // 使用client  执行搜索
        SearchResponse searchResponse  = client.search(searchRequest,RequestOptions.DEFAULT);

        // 搜索结果
        SearchHits  hits = searchResponse.getHits();
        // 匹配到的总记录数
        TotalHits  totalHits  = hits.getTotalHits();
        System.out.println("查询到的总记录数:"+totalHits.value);
        // 得到的匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit  hit : searchHits){
            String  id = hit.getId();
            // 源文档的内容
            Map<String,Object>  sourceMap = hit.getSourceAsMap();
            String  name  = (String)sourceMap.get("name");
            String  timestamp  = (String)sourceMap.get("timestamp");
            String  description  = (String)sourceMap.get("description");
            Double   price  = (Double)sourceMap.get("price");
            System.out.println(name);
            System.out.println(timestamp);
            System.out.println(description);
            System.out.println(price);
        }
    }
    @Test
    public  void   testSearchAllPage()throws  IOException{
        // 搜索请求对象
        SearchRequest  searchRequest = new SearchRequest("elasticsearch_test");
        // 搜索源构建对象
        SearchSourceBuilder  searchSourceBuilder = new SearchSourceBuilder();
        // 设置搜索方法
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.fetchSource(new String[]{"name","price","timestamp"},new String[]{});
        // 设置分页参数
        int  page = 2;
        int  size = 2;
        // 计算出 from
        int  form = (page-1)*size;
        searchSourceBuilder.from(form);
        searchSourceBuilder.size(size);
        // 设置price 降序
        searchSourceBuilder.sort("price",SortOrder.DESC);
        // 请求对象设置 搜索源对象
        searchRequest.source(searchSourceBuilder);
        // 使用client  执行搜索
        SearchResponse searchResponse  = client.search(searchRequest,RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits  hits = searchResponse.getHits();
        // 匹配到的总记录数
        TotalHits  totalHits  = hits.getTotalHits();
        System.out.println("查询到的总记录数:"+totalHits.value);
        // 得到的匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit  hit : searchHits){
            String  id = hit.getId();
            // 源文档的内容
            Map<String,Object>  sourceMap = hit.getSourceAsMap();
            String  name  = (String)sourceMap.get("name");
            String  timestamp  = (String)sourceMap.get("timestamp");
            String  description  = (String)sourceMap.get("description");
            Double   price  = (Double)sourceMap.get("price");
            System.out.println(name);
            System.out.println(timestamp);
            System.out.println(description);
            System.out.println(price);
        }
    }
    @Test
    public  void   testTermQueryPage()throws  IOException{
        // 搜索请求对象
        SearchRequest  searchRequest = new SearchRequest("elasticsearch_test");
        // 搜索源构建对象
        SearchSourceBuilder  searchSourceBuilder = new SearchSourceBuilder();
        // 设置搜索方法
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring cloud实战"));
        searchSourceBuilder.fetchSource(new String[]{"name","price","timestamp"},new String[]{});
        // 设置分页参数
        int  page = 1;
        int  size = 2;
        // 计算出 from
        int  form = (page-1)*size;
        searchSourceBuilder.from(form);
        searchSourceBuilder.size(size);
        // 设置price 降序
        searchSourceBuilder.sort("price",SortOrder.DESC);
        // 请求对象设置 搜索源对象
        searchRequest.source(searchSourceBuilder);
        // 使用client  执行搜索
        SearchResponse searchResponse  = client.search(searchRequest,RequestOptions.DEFAULT);
        // 搜索结果
        SearchHits  hits = searchResponse.getHits();
        // 匹配到的总记录数
        TotalHits  totalHits  = hits.getTotalHits();
        System.out.println("查询到的总记录数:"+totalHits.value);
        // 得到的匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit  hit : searchHits){
            String  id = hit.getId();
            // 源文档的内容
            Map<String,Object>  sourceMap = hit.getSourceAsMap();
            String  name  = (String)sourceMap.get("name");
            String  timestamp  = (String)sourceMap.get("timestamp");
            String  description  = (String)sourceMap.get("description");
            Double   price  = (Double)sourceMap.get("price");
            System.out.println(name);
            System.out.println(timestamp);
            System.out.println(description);
            System.out.println(price);
        }
    }

    public List<Map<String,Object> > searchQuestionBO(String scrollId){
        // 搜索请求对象
        SearchRequest  searchRequest = new SearchRequest("elasticsearch_test");
        // 搜索源构建对象
        SearchSourceBuilder  searchSourceBuilder = new SearchSourceBuilder();
        // 设置搜索方法
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring cloud实战"));
        searchSourceBuilder.fetchSource(new String[]{"name","price","timestamp"},new String[]{});
        // 设置price 降序
        searchSourceBuilder.sort("price",SortOrder.DESC);
        // 请求对象设置 搜索源对象
        searchRequest.source(searchSourceBuilder);
        List<Map<String,Object> > questionBOList = new ArrayList<>();
        SearchResponse searchResponse = null;
        boolean deepSearch = false;
        try {

            deepSearch = true;
            searchRequest.source().size(2);
            final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(30L));
            searchRequest.scroll(scroll);
            searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest,RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            SearchHit[] searchHit = searchHits.getHits();
            long total = searchHits.getTotalHits().value;
            for (int i = 0;i < searchHit.length;i++) {
                SearchHit item = searchHit[i];
                questionBOList.add(item.getSourceAsMap());
            }
            return questionBOList;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questionBOList;
    }
}





