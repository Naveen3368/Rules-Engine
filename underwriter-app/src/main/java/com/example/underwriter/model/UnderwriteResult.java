package com.example.underwriter.model;

import java.util.ArrayList;
import java.util.List;

public class UnderwriteResult {
  public enum Status { APPROVED, REJECTED, MANUAL_REVIEW }
  private Status status;
  private List<String> reasons = new ArrayList<>();
  public Status getStatus(){return status;} public void setStatus(Status s){status=s;}
  public List<String> getReasons(){return reasons;} public void setReasons(List<String> r){reasons=r;}
  public void addReason(String r){reasons.add(r);}
}
