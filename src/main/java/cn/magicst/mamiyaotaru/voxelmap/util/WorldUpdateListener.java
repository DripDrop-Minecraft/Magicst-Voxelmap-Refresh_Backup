 package cn.magicst.mamiyaotaru.voxelmap.util;
 
 import cn.magicst.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
 import java.util.ArrayList;
 import java.util.List;
 
 public class WorldUpdateListener
 {
   private final List<IChangeObserver> chunkProcessors = new ArrayList<>();
   
   public void addListener(IChangeObserver chunkProcessor) {
     this.chunkProcessors.add(chunkProcessor);
   }
   
   public void notifyObservers(int chunkX, int chunkZ) {
     try {
       for (IChangeObserver chunkProcessor : this.chunkProcessors) {
         chunkProcessor.handleChangeInWorld(chunkX, chunkZ);
       }
     } catch (Exception e) {
       e.printStackTrace();
     } 
   }
 }
