 package cn.magicst.mamiyaotaru.voxelmap.util;
 
 import cn.magicst.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
 import net.minecraft.class_2338;
 import net.minecraft.class_2791;
 import net.minecraft.class_2818;
 import net.minecraft.class_310;
 
 public class MapChunkCache {
   private int width;
   private int height;
   private class_2818 lastCenterChunk = null;
   private MapChunk[] mapChunks;
   private int left = 0;
   private int right = 0;
   private int top = 0;
   private int bottom = 0;
   private boolean loaded = false;
   private IChangeObserver changeObserver;
   
   public MapChunkCache(int width, int height, IChangeObserver changeObserver) {
     this.width = width;
     this.height = height;
     this.mapChunks = new MapChunk[width * height];
     this.changeObserver = changeObserver;
   }
   
   public void centerChunks(class_2338 blockPos) {
     class_2818 currentChunk = (class_310.method_1551()).field_1687.method_8500(blockPos);
     if (currentChunk != this.lastCenterChunk) {
       if (this.lastCenterChunk == null) {
         fillAllChunks(blockPos);
         this.lastCenterChunk = currentChunk;
         
         return;
       } 
       int middleX = this.width / 2;
       int middleZ = this.height / 2;
       int movedX = (currentChunk.method_12004()).field_9181 - (this.lastCenterChunk.method_12004()).field_9181;
       int movedZ = (currentChunk.method_12004()).field_9180 - (this.lastCenterChunk.method_12004()).field_9180;
       if (Math.abs(movedX) < this.width && Math.abs(movedZ) < this.height && currentChunk.method_12200().equals(this.lastCenterChunk.method_12200()))
       { moveX(movedX);
         moveZ(movedZ);
         int z;
         for (z = (movedZ > 0) ? (this.height - movedZ) : 0; z < ((movedZ > 0) ? this.height : -movedZ); z++) {
           for (int x = 0; x < this.width; x++) {
             this.mapChunks[x + z * this.width] = new MapChunk((currentChunk.method_12004()).field_9181 - middleX - x, (currentChunk.method_12004()).field_9180 - middleZ - z);
           }
         } 
         
         for (z = 0; z < this.height; ) {
           int x = (movedX > 0) ? (this.width - movedX) : 0; for (;; z++) { if (x < ((movedX > 0) ? this.width : -movedX)) {
               this.mapChunks[x + z * this.width] = new MapChunk((currentChunk.method_12004()).field_9181 - middleX - x, (currentChunk.method_12004()).field_9180 - middleZ - z); x++; continue;
             }  }
         
         }  }
       else { fillAllChunks(blockPos); }
 
       
       this.left = this.mapChunks[0].getX();
       this.top = this.mapChunks[0].getZ();
       this.right = this.mapChunks[this.mapChunks.length - 1].getX();
       this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
       this.lastCenterChunk = currentChunk;
     } 
   }
 
   
   private void fillAllChunks(class_2338 blockPos) {
     class_2791 currentChunk = (class_310.method_1551()).field_1687.method_22350(blockPos);
     int middleX = this.width / 2;
     int middleZ = this.height / 2;
     
     for (int z = 0; z < this.height; z++) {
       for (int x = 0; x < this.width; x++) {
         this.mapChunks[x + z * this.width] = new MapChunk((currentChunk.method_12004()).field_9181 - middleX - x, (currentChunk.method_12004()).field_9180 - middleZ - z);
       }
     } 
     
     this.left = this.mapChunks[0].getX();
     this.top = this.mapChunks[0].getZ();
     this.right = this.mapChunks[this.mapChunks.length - 1].getX();
     this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
     this.loaded = true;
   }
   
   private void moveX(int offset) {
     if (offset > 0) {
       System.arraycopy(this.mapChunks, offset, this.mapChunks, 0, this.mapChunks.length - offset);
     } else if (offset < 0) {
       System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset, this.mapChunks.length + offset);
     } 
   }
 
   
   private void moveZ(int offset) {
     if (offset > 0) {
       System.arraycopy(this.mapChunks, offset * this.width, this.mapChunks, 0, this.mapChunks.length - offset * this.width);
     } else if (offset < 0) {
       System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset * this.width, this.mapChunks.length + offset * this.width);
     } 
   }
 
   
   public void checkIfChunksChanged() {
     if (this.loaded) {
       for (int z = this.height - 1; z >= 0; z--) {
         for (int x = 0; x < this.width; x++) {
           this.mapChunks[x + z * this.width].checkIfChunkChanged(this.changeObserver);
         }
       } 
     }
   }
 
   
   public void checkIfChunksBecameSurroundedByLoaded() {
     if (this.loaded) {
       for (int z = this.height - 1; z >= 0; z--) {
         for (int x = 0; x < this.width; x++) {
           this.mapChunks[x + z * this.width].checkIfChunkBecameSurroundedByLoaded(this.changeObserver);
         }
       } 
     }
   }
 
   
   public void registerChangeAt(int chunkX, int chunkZ) {
     try {
       if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
         int arrayX = chunkX - this.left;
         int arrayZ = chunkZ - this.top;
         MapChunk mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
         mapChunk.setModified(true);
       } 
     } catch (Exception e) {
       e.printStackTrace();
     } 
   }
   
   public boolean isChunkSurroundedByLoaded(int chunkX, int chunkZ) {
     if (this.lastCenterChunk != null && chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
       int arrayX = chunkX - this.left;
       int arrayZ = chunkZ - this.top;
       MapChunk mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
       return mapChunk.isSurroundedByLoaded();
     } 
     return false;
   }
 }
 