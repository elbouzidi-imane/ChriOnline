package com.chrionline.client.service;

import com.chrionline.client.model.CategoryDTO;
import com.chrionline.client.model.ProductDTO;
import com.chrionline.client.network.TCPClient;
import com.chrionline.client.util.JsonUtils;
import com.chrionline.common.Message;
import com.chrionline.common.Protocol;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ProductService {
    private final TCPClient tcp = TCPClient.getInstance();

    public List<ProductDTO> getAll() throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_PRODUCTS, ""));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        Type listType = new TypeToken<List<ProductDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public List<CategoryDTO> getCategories() throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_CATEGORIES, ""));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        Type listType = new TypeToken<List<CategoryDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }

    public ProductDTO getById(int id) throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_PRODUCT, String.valueOf(id)));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        return JsonUtils.GSON.fromJson(response.getPayload(), ProductDTO.class);
    }

    public List<ProductDTO> getByCategory(int categoryId) throws Exception {
        Message response = tcp.send(new Message(Protocol.GET_PRODUCTS_BY_CATEGORIE, String.valueOf(categoryId)));
        if (response.isError()) {
            throw new IllegalStateException(response.getPayload());
        }
        Type listType = new TypeToken<List<ProductDTO>>() { }.getType();
        return JsonUtils.GSON.fromJson(response.getPayload(), listType);
    }
}
