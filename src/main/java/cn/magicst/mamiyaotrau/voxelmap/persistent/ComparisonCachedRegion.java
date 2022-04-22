 package cn.magicst.mamiyaotaru.voxelmap.persistent;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import cn.magicst.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;
 import cn.magicst.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
 import cn.magicst.mamiyaotaru.voxelmap.util.BlockStateParser;
 import cn.magicst.mamiyaotaru.voxelmap.util.CommandUtils;
 import cn.magicst.mamiyaotaru.voxelmap.util.MessageUtils;
 import cn.magicst.mamiyaotaru.voxelmap.util.MutableBlockPos;
 import cn.magicst.mamiyaotaru.voxelmap.util.TextUtils;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Scanner;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import net.minecraft.class_1937;
 import net.minecraft.class_2818;
 import net.minecraft.class_2902;
 import net.minecraft.class_310;
 import net.minecraft.class_638;
 
 public class ComparisonCachedRegion {
   private IPersistentMap persistentMap;
   private String key;
   private class_638 world;
   private String worldName;
   private String subworldName;
   private String worldNamePathPart;
   private String subworldNamePathPart;
   private String dimensionNamePathPart;
   private boolean underground;
   private int x;
   private int z;
   private CompressibleMapData data;
   MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
   private int loadedChunks = 0;
   private boolean loaded = false;
   private boolean empty = true;
   
   public ComparisonCachedRegion(IPersistentMap persistentMap, String key, class_638 world, String worldName, String subworldName, int x, int z) {
     this.data = new CompressibleMapData(256, 256);
     this.persistentMap = persistentMap;
     this.key = key;
     this.world = world;
     this.worldName = worldName;
     this.subworldName = subworldName;
     this.worldNamePathPart = TextUtils.scrubNameFile(worldName);
     if (subworldName != "") {
       this.subworldNamePathPart = TextUtils.scrubNameFile(subworldName) + "/";
     }
     
     String dimensionName = AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByWorld((class_1937)world).getStorageName();
     this.dimensionNamePathPart = TextUtils.scrubNameFile(dimensionName);
     boolean knownUnderground = false;
     this.underground = ((!world.method_28103().method_28114() && !world.method_8597().method_12491()) || world.method_8597().method_27998() || knownUnderground);
     this.x = x;
     this.z = z;
   }
   
   public void loadCurrent() {
     this.loadedChunks = 0;
     
     for (int chunkX = 0; chunkX < 16; chunkX++) {
       for (int chunkZ = 0; chunkZ < 16; chunkZ++) {
         class_2818 chunk = this.world.method_8497(this.x * 16 + chunkX, this.z * 16 + chunkZ);
         if (chunk != null && !chunk.method_12223() && this.world.method_8393(this.x * 16 + chunkX, this.z * 16 + chunkZ) && !isChunkEmpty(this.world, chunk)) {
           loadChunkData(chunk, chunkX, chunkZ);
           this.loadedChunks++;
         } 
       } 
     } 
   }
 
   
   private boolean isChunkEmpty(class_638 world, class_2818 chunk) {
     for (int t = 0; t < 16; t++) {
       for (int s = 0; s < 16; s++) {
         if (chunk.method_12005(class_2902.class_2903.field_13197, t, s) != 0) {
           return false;
         }
       } 
     } 
     
     return true;
   }
   
   private void loadChunkData(class_2818 chunk, int chunkX, int chunkZ) {
     for (int t = 0; t < 16; t++) {
       for (int s = 0; s < 16; s++) {
         this.persistentMap.getAndStoreData(this.data, (class_1937)this.world, chunk, this.blockPos, this.underground, this.x * 256, this.z * 256, chunkX * 16 + t, chunkZ * 16 + s);
       }
     } 
   }
 
   
   public void loadStored() {
     try {
       File cachedRegionFileDir = new File((class_310.method_1551()).field_1697, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
       cachedRegionFileDir.mkdirs();
       File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
       if (cachedRegionFile.exists()) {
         HashBiMap hashBiMap; FileInputStream fis = new FileInputStream(cachedRegionFile);
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
         Scanner sc = new Scanner(zis);
         BiMap stateToInt = null;
         int version = 1;
         int total = 0;
         
         ZipEntry ze;
         byte[] decompressedByteData;
         for (decompressedByteData = new byte[this.data.getWidth() * this.data.getHeight() * 17 * 4]; (ze = zis.getNextEntry()) != null; zis.closeEntry()) {
           
           if (ze.getName().equals("data")) {
             int count; for (byte[] data = new byte[2048]; (count = zis.read(data, 0, 2048)) != -1 && count + total <= this.data.getWidth() * this.data.getHeight() * 17 * 4; total += count) {
               System.arraycopy(data, 0, decompressedByteData, total, count);
             }
           } 
           
           if (ze.getName().equals("key")) {
             hashBiMap = HashBiMap.create();
             
             while (sc.hasNextLine()) {
               BlockStateParser.parseLine(sc.nextLine(), (BiMap)hashBiMap);
             }
           } 
           
           if (ze.getName().equals("control")) {
             Properties properties = new Properties();
             properties.load(zis);
             String versionString = properties.getProperty("version", "1");
             
             try {
               version = Integer.parseInt(versionString);
             } catch (NumberFormatException var14) {
               version = 1;
             } 
           } 
         } 
         
         if (total == this.data.getWidth() * this.data.getHeight() * 18 && hashBiMap != null) {
           byte[] byteData = new byte[this.data.getWidth() * this.data.getHeight() * 18];
           System.arraycopy(decompressedByteData, 0, byteData, 0, byteData.length);
           this.data.setData(byteData, (BiMap)hashBiMap, version);
           this.empty = false;
           this.loaded = true;
         } else {
           System.out.println("failed to load data from " + cachedRegionFile.getPath());
         } 
         
         sc.close();
         zis.close();
         fis.close();
       } 
     } catch (IOException var15) {
       System.err.println("Failed to load region file for " + this.x + "," + this.z + " in " + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart);
       var15.printStackTrace();
     } 
   }
 
   
   public String getSubworldName() {
     return this.subworldName;
   }
   
   public String getKey() {
     return this.key;
   }
   
   public CompressibleMapData getMapData() {
     return this.data;
   }
   
   public boolean isLoaded() {
     return this.loaded;
   }
   
   public boolean isEmpty() {
     return this.empty;
   }
   
   public int getLoadedChunks() {
     return this.loadedChunks;
   }
   
   public boolean isGroundAt(int blockX, int blockZ) {
     return (isLoaded() && getHeightAt(blockX, blockZ) > 0);
   }
   
   public int getHeightAt(int blockX, int blockZ) {
     int x = blockX - this.x * 256;
     int z = blockZ - this.z * 256;
     int y = this.data.getHeight(x, z);
     if (this.underground && y == 255) {
       y = CommandUtils.getSafeHeight(blockX, 64, blockZ, (class_1937)this.world);
     }
     
     return y;
   }
   
   public int getSimilarityTo(ComparisonCachedRegion candidate) {
     int compared = 0;
     int matched = 0;
     CompressibleMapData candidateData = candidate.getMapData();
     
     for (int t = 0; t < 16; t++) {
       for (int s = 0; s < 16; s++) {
         int nonZeroHeights = 0;
         int nonZeroHeightsInCandidate = 0;
         int matchesInChunk = 0;
         
         for (int i = 0; i < 16; i++) {
           for (int j = 0; j < 16; j++) {
             int x = t * 16 + i;
             int z = s * 16 + j;
             if (this.data.getHeight(x, z) == candidateData.getHeight(x, z) && this.data.getBlockstate(x, z) == candidateData.getBlockstate(x, z) && (this.data.getOceanFloorHeight(x, z) == 0 || (this.data.getOceanFloorHeight(x, z) == candidateData.getOceanFloorHeight(x, z) && this.data.getOceanFloorBlockstate(x, z) == candidateData.getOceanFloorBlockstate(x, z)))) {
               matchesInChunk++;
             }
             
             if (this.data.getHeight(x, z) != 0) {
               nonZeroHeights++;
             }
             
             if (candidateData.getHeight(x, z) != 0) {
               nonZeroHeightsInCandidate++;
             }
           } 
         } 
         
         if (nonZeroHeights != 0 && nonZeroHeightsInCandidate != 0) {
           compared += 256;
           matched += matchesInChunk;
         } 
         
         MessageUtils.printDebug("at " + t + "," + s + " there were local non zero: " + nonZeroHeights + " and comparison non zero: " + nonZeroHeightsInCandidate);
       } 
     } 
     
     MessageUtils.printDebug("compared: " + compared + ", matched: " + matched);
     return (compared >= 256) ? (matched * 100 / compared) : 0;
   }
 }

