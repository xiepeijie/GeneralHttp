package com.example.payge.network.response;

import com.example.payge.network.model.Article;

import java.util.List;

public class ArticleListResponse {

    public DataEntity data;

    public static class DataEntity {
        public List<Article> datas;
    }

}
