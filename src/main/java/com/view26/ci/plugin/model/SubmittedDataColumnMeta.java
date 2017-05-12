package com.view26.ci.plugin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.tasks.junit.CaseResult;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author aneeshia
 * Created on 3/5/17.
 */
public class SubmittedDataColumnMeta {
    @JsonProperty("name")
    private String name;
    @JsonProperty("type")
    private String type;
    @JsonProperty("v26Field")
    private String v26Field;
    @JsonProperty("label")
    private String label;
    @JsonProperty("list")
    private String[] list;

    public SubmittedDataColumnMeta(String name, String label, String v26Field, String type){
        this.name = name;
        this.label = label;
        this.v26Field = v26Field;
        this.type = type;

        // Initialise list of possible values
        String[] listItems = null;

        switch (name){
            case "status":
                listItems = new String[] {};
                for(CaseResult.Status status : CaseResult.Status.values()){
                    listItems = append(listItems, status.getMessage().toUpperCase());
                }
                break;
            case "exec_type":listItems = new String[] {"Functional","Regression","Non Functional","Sanity","Smoke"};break;
            case "test_type":listItems = new String[] {"Manual","Automation"};break;
        }

        if(listItems != null){
            this.list = listItems;
        }

        return;
    }

    static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }
}
