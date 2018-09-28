package com.dianping.cat.configuration.property.transform;

import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;

import java.util.ArrayList;
import java.util.List;

public class DefaultLinker implements ILinker {
   private boolean deferrable;
   private List<Runnable> deferedJobs = new ArrayList<Runnable>();

   public DefaultLinker(boolean deferrable) {
      this.deferrable = deferrable;
   }

   public void finish() {
      for (Runnable job : deferedJobs) {
         job.run();
      }
   }

   @Override
   public boolean onProperty(final PropertyConfig parent, final Property property) {
      if (deferrable) {
         deferedJobs.add(new Runnable() {
            @Override
            public void run() {
               parent.addProperty(property);
            }
         });
      } else {
         parent.addProperty(property);
      }

      return true;
   }
}
