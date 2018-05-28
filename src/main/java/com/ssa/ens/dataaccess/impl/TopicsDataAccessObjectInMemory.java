package com.ssa.ens.dataaccess.impl;

import com.ssa.ens.dataaccess.TopicsDataAccessObject;
import com.ssa.ens.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class TopicsDataAccessObjectInMemory implements TopicsDataAccessObject
{
   private static final List<Topic>  IN_MEM_TOPICS_DB = new ArrayList<Topic>();

   static {
       IN_MEM_TOPICS_DB.add(new Topic(1, "blood_donation"));
       IN_MEM_TOPICS_DB.add(new Topic(2, "cardiac_arrest"));
       IN_MEM_TOPICS_DB.add(new Topic(3, "t1d-hypoglycemia"));
       IN_MEM_TOPICS_DB.add(new Topic(4, "craving-for-chocolate"));
   }


    public List<Topic> getTopics()
    {
        return  IN_MEM_TOPICS_DB;
    }
}
