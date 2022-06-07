 package cn.magicst.mamiyaotaru.voxelmap.util;
 
 import cn.magicst.mamiyaotaru.voxelmap.interfaces.IGLBufferedImage;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 
 public class GLBufferedImage
   extends BufferedImage implements IGLBufferedImage {
   protected ByteBuffer buffer;
   protected byte[] bytes;
   protected int index = 0;
   protected Object bufferLock = new Object();
   
   public GLBufferedImage(int width, int height, int imageType) {
     super(width, height, imageType);
     this.bytes = ((DataBufferByte)getRaster().getDataBuffer()).getData();
     this.buffer = ByteBuffer.allocateDirect(this.bytes.length).order(ByteOrder.nativeOrder());
   }
 
   
   public int getIndex() {
     return this.index;
   }
 
   
   public void baleet() {
     int currentIndex = this.index;
     this.index = 0;
     if (currentIndex != 0) {
       GLShim.glDeleteTextures(currentIndex);
     }
   }
 
 
   
   public void write() {
     if (this.index == 0) {
       this.index = GLShim.glGenTextures();
     }
     
     this.buffer.clear();
     synchronized (this.bufferLock) {
       this.buffer.put(this.bytes);
     } 
     
     this.buffer.position(0).limit(this.bytes.length);
     GLShim.glBindTexture(3553, this.index);
     GLShim.glTexParameteri(3553, 10241, 9728);
     GLShim.glTexParameteri(3553, 10240, 9728);
     GLShim.glTexParameteri(3553, 10242, 33071);
     GLShim.glTexParameteri(3553, 10243, 33071);
     GLShim.glPixelStorei(3314, 0);
     GLShim.glPixelStorei(3316, 0);
     GLShim.glPixelStorei(3315, 0);
     GLShim.glTexImage2D(3553, 0, 6408, getWidth(), getHeight(), 0, 6408, 32821, this.buffer);
   }
 
   
   public void blank() {
     Arrays.fill(this.bytes, (byte)0);
     write();
   }
 
   
   public void setRGB(int x, int y, int color24) {
     int index = (x + y * getWidth()) * 4;
     synchronized (this.bufferLock) {
       this.bytes[index] = (byte)(color24 >> 24);
       this.bytes[index + 1] = (byte)(color24 >> 0);
       this.bytes[index + 2] = (byte)(color24 >> 8);
       this.bytes[index + 3] = (byte)(color24 >> 16);
     } 
   }
 
   
   public void moveX(int offset) {
     synchronized (this.bufferLock) {
       if (offset > 0) {
         System.arraycopy(this.bytes, offset * 4, this.bytes, 0, this.bytes.length - offset * 4);
       } else if (offset < 0) {
         System.arraycopy(this.bytes, 0, this.bytes, -offset * 4, this.bytes.length + offset * 4);
       } 
     } 
   }
 
 
   
   public void moveY(int offset) {
     synchronized (this.bufferLock) {
       if (offset > 0) {
         System.arraycopy(this.bytes, offset * getWidth() * 4, this.bytes, 0, this.bytes.length - offset * getWidth() * 4);
       } else if (offset < 0) {
         System.arraycopy(this.bytes, 0, this.bytes, -offset * getWidth() * 4, this.bytes.length + offset * getWidth() * 4);
       } 
     } 
   }
 }
