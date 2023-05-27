package com.example.services.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    //todo add test
    public static <T> List<T> getEveryNBasedOnLimit(List<T> inputList, int limit) {
        if (inputList.size() % limit != 0) {
            throw new IllegalArgumentException("Input list size is not divisible by the limit.");
        }

        List<T> result = new ArrayList<>();

        int x = inputList.size() / limit;

        for (int i = 0; i < inputList.size(); i += x) {
            result.add(inputList.get(i));
        }

        return result;
    }
}
