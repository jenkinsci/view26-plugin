package com.view26.ci.plugin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by actio9 on 3/5/17.
 */
public class SubmittedData {

    @JsonProperty("columns")
    private ArrayList<SubmittedDataColumnMeta> columns;

    @JsonProperty("dsAPIKey")
    private String apiKey = null;

    @JsonProperty("data")
    private ArrayList<ArrayList> data;

    public SubmittedData(){

        columns = new ArrayList<SubmittedDataColumnMeta>();

        columns.add(new SubmittedDataColumnMeta("name","Name",null,"String"));
        columns.add(new SubmittedDataColumnMeta("jenkins_test_set","Jenkins Test Set",null,"String"));
        columns.add(new SubmittedDataColumnMeta("status","Status","status","LookupList"));
        columns.add(new SubmittedDataColumnMeta("exe_start_date","Execution Start Date","exec-date","Date"));
        columns.add(new SubmittedDataColumnMeta("exe_end_date","Execution End Date",null,"Date"));
        columns.add(new SubmittedDataColumnMeta("automation_content","Automation Content",null,"String"));
        columns.add(new SubmittedDataColumnMeta("buildNumber","Build Number",null,"Number"));
        columns.add(new SubmittedDataColumnMeta("buildPath","Build Path",null,"String"));
        columns.add(new SubmittedDataColumnMeta("assignee","Assignee","owner","UsersList"));
        columns.add(new SubmittedDataColumnMeta("release_name","Release",null,"String"));
        columns.add(new SubmittedDataColumnMeta("duration","Time",null,"String"));

        columns.add(new SubmittedDataColumnMeta("exec_type","Execution Type",null,"LookupList"));
        columns.add(new SubmittedDataColumnMeta("test_type","Test Type",null,"LookupList"));
        columns.add(new SubmittedDataColumnMeta("priority","Priority",null,"String"));
        columns.add(new SubmittedDataColumnMeta("ci_tool","CI Tool",null,"String"));
    }

    public void setApiKey(String apiKey){
        this.apiKey = apiKey;
    }

    public void setData(String userName, String projectName, String buildNumber, String buildPath, List<AutomationTestResult> testResults, Configuration configuration){

        // set api key
        setApiKey(configuration.getAppSecretKey());

        data = new ArrayList<ArrayList>();

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        for (AutomationTestResult item : testResults){

            ArrayList<String> row = new ArrayList<String>();

            row.add(item.getName());
            row.add(projectName+"_"+timeStamp);
            row.add(item.getStatus());
            row.add(item.getExecutedStartDate().toString());
            row.add(item.getExecutedEndDate().toString());
            row.add(item.getAutomationContent());
            row.add(buildNumber);
            row.add(buildPath);
            row.add(userName);
            row.add(configuration.getReleaseName());
            row.add(item.getDuration().toString());

            row.add(item.getExecutionType());
            row.add(item.getTestType());
            row.add(item.getPriority());
            row.add(item.getCiTool());


            data.add(row);

        }
    }

}
