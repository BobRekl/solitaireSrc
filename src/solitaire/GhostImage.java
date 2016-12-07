/*
*
* Solitaire game - Ghost Image.
*
*/
package solitaire;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * Creates a transparent ghost image
 */
public class GhostImage {
    BufferedImage ghost;
    
    GhostImage(BufferedImage img, float iTransparency){
        BufferedImage imageCopy;
        
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        
        //Create a rescale filter op that makes the image transparent
        imageCopy = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics gg = imageCopy.createGraphics();
        gg.drawImage(img, 0, 0, null); //an invocation of the gods of JAVA that may copy the image
        gg.dispose();
        float[] scales = { 1f, 1f, 1f, 0.5f };
        scales[3] = iTransparency; //sets transparency 0 is invisible 1 is opaque
        float[] offsets = new float[4];
        RescaleOp rop = new RescaleOp(scales, offsets, null);
        ghost = rop.filter(imageCopy, null);
        //System.out.println("DemoImage ghost width = " +img.getWidth()+", ghost height = "+ img.getHeight());
    }
    
    BufferedImage getGhostImage(){
        return ghost;
    }
}
