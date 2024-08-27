/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbj?rn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.util.Map;

import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.FSFontMetrics;
import org.xhtmlrenderer.render.JustificationInfo;
import org.xhtmlrenderer.render.LineMetricsAdapter;
import org.xhtmlrenderer.util.Configuration;


/**
 * Renders to a Graphics2D instance.
 *
 * @author   Joshua Marinacci
 * @author   Torbjoern Gannholm
 */
public class Java2DTextRenderer implements TextRenderer {
    protected float scale;
    protected float threshold;
    protected Object antiAliasRenderingHint;

    public Java2DTextRenderer() {
        scale = Configuration.valueAsFloat("xr.text.scale", 1.0f);
        threshold = Configuration.valueAsFloat("xr.text.aa-fontsize-threshhold", 25);

        Object dummy = new Object();

        Object aaHint = Configuration.valueFromClassConstant("xr.text.aa-rendering-hint", dummy);
        if (aaHint == dummy) {
            try {
                Map map;
                // we should be able to look up the "recommended" AA settings (that correspond to the user's
                // desktop preferences and machine capabilities
                // see: http://java.sun.com/javase/6/docs/api/java/awt/doc-files/DesktopProperties.html
                Toolkit tk = Toolkit.getDefaultToolkit();
                map = (Map) (tk.getDesktopProperty("awt.font.desktophints"));
                antiAliasRenderingHint = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            } catch (Exception e) {
                // conceivably could get an exception in a webstart environment? not sure
                antiAliasRenderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            }
        } else {
            antiAliasRenderingHint = aaHint;
        }
    }

    /**
     * 根据指定宽度自动换行
     * @param g
     * @param maxWdith
     * @param strContent
     * @param loc_X
     * @param loc_Y
     * @param font
     */
    private  int  drawStringWithFontStyleLineFeed(Graphics2D g, String strContent,int maxWdith, int loc_X, int loc_Y, Font font){
        g.setFont(font);
        //获取字符串 字符的总宽度
        int strWidth =getStringLength(g,strContent);
        //每一行字符串宽度
        int rowWidth=maxWdith;
        // System.out.println("每行字符宽度:"+rowWidth);
        //获取字符高度
        int strHeight=getStringHeight(g);
        int totalHeight = strHeight;
        //字符串总个数
        //System.out.println("字符串总个数:"+strContent.length()+"======>"+strContent);
        if(strWidth>rowWidth){
            char[] strContentArr = strContent.toCharArray();
            int count = 0;
            int conut_value = 0;
            int line = 0;
            int charWidth = 0;
            for(int j=0;j< strContentArr.length;j++){
                if(conut_value+g.getFontMetrics().charWidth(strContentArr[j])>=rowWidth){
                    conut_value = 0;
                    //System.out.println("绘制1:"+strContent.substring(count,j));
                    g.drawString(strContent.substring(count,j),loc_X,loc_Y+strHeight*line);
                    count = j;
                    line++;
                    totalHeight += strHeight;
                }else{
                    if(j==strContentArr.length - 1){
                        //System.out.println("绘制2:"+strContent.substring(count,j+1));
                        g.drawString(strContent.substring(count,j+1),loc_X,loc_Y+strHeight*line);
                    }else{
                        charWidth = g.getFontMetrics().charWidth(strContentArr[j]);
                        conut_value = charWidth + conut_value;
                    }
                }
            }

        }else{
            //直接绘制
            g.drawString(strContent, loc_X, loc_Y);
        }
        //System.out.println("打印总高度:" + totalHeight);
        return totalHeight;
    }
    private int  getStringLength(Graphics2D g,String str) {
        char[]  strcha=str.toCharArray();
        int strWidth = g.getFontMetrics().charsWidth(strcha, 0, str.length());
        //System.out.println("字符总宽度:"+strWidth);
        return strWidth;
    }


    private int  getStringHeight(Graphics2D g) {
        int height = g.getFontMetrics().getHeight();
        //System.out.println("字符高度:"+height);
        return height;
    }

    int _printHeight = 0;

    public int getPrintHeight() {
        return _printHeight;
    }

    int _needLine = 1;

    public int getNeedLine() {
        return _needLine;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public void drawString(OutputDevice outputDevice, String string, float x, float y ) {
        //System.out.println("print string 1 " + string + "====" + x + ":" + y);
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        if ( graphics.getFont().getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        if (_width <= 0) {
            _printHeight = getStringHeight(graphics);
            graphics.drawString(string, (int) x, (int) y);
        } else {
            _printHeight = drawStringWithFontStyleLineFeed(graphics, string, _width, (int) x, (int) y, graphics.getFont());
        }
        if ( graphics.getFont().getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
    }
    
    public void drawString(
            OutputDevice outputDevice, String string, float x, float y, JustificationInfo info) {
        //System.out.println("print string 2 " + string + "====" + x + ":" + y);
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        if ( graphics.getFont().getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        
        GlyphVector vector = graphics.getFont().createGlyphVector(
                graphics.getFontRenderContext(), string);
        
        adjustGlyphPositions(string, info, vector);
        
        graphics.drawGlyphVector(vector, x, y);
        
        if ( graphics.getFont().getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
    }

    private void adjustGlyphPositions(
            String string, JustificationInfo info, GlyphVector vector) {
        float adjust = 0.0f;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i != 0) {
                Point2D point = vector.getGlyphPosition(i);
                vector.setGlyphPosition(
                        i, new Point2D.Double(point.getX() + adjust, point.getY()));
            }
            if (c == ' ' || c == '\u00a0' || c == '\u3000') {
                adjust += info.getSpaceAdjust();
            } else {
                adjust += info.getNonSpaceAdjust();
            }
        }
    }
    
    public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector fsGlyphVector, float x, float y ) {
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        
        if ( graphics.getFont().getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        GlyphVector vector = ((AWTFSGlyphVector)fsGlyphVector).getGlyphVector();
        graphics.drawGlyphVector(vector, (int)x, (int)y );
        if ( graphics.getFont().getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
    }

    /** {@inheritDoc} */
    public void setup(FontContext fontContext) {
        //Uu.p("setup graphics called");
        ((Java2DFontContext)fontContext).getGraphics().setRenderingHint( 
                RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF );
    }

    public void setFontScale( float scale ) {
        this.scale = scale;
    }

    public void setSmoothingThreshold( float fontsize ) {
        threshold = fontsize;
    }

    public void setSmoothingLevel( int level ) { /* no-op */ }

    public FSFontMetrics getFSFontMetrics(FontContext fc, FSFont font, String string ) {
        Graphics2D graphics = ((Java2DFontContext)fc).getGraphics();
        return new LineMetricsAdapter(
                ((AWTFSFont)font).getAWTFont().getLineMetrics(
                        string, graphics.getFontRenderContext()));
    }

    private int _width = 0;

    public TextRenderer setWidth(int width) {
        _width = width;
        if (_width == 0) {
            _printHeight = 0;
            _needLine = 0;
        }
        return this;
    }
    public int getWidth(FontContext fc, FSFont font, String string) {
        Graphics2D graphics = ((Java2DFontContext)fc).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        int result = (int)Math.ceil(
                graphics.getFontMetrics(awtFont).getStringBounds(string, graphics).getWidth());
        _printHeight = graphics.getFontMetrics().getHeight();
        if (_width >0 && result > _width) {
            //System.out.println("获取宽度:" + result + "========>" + _width + "|||" + string);
            _needLine = (result + _width - 1) / _width;
            _printHeight = _printHeight * _needLine;
            result = _width;
        }
        return result;
    }

    public float getFontScale() {
        return this.scale;
    }

    public int getSmoothingLevel() {
        return 0;
    }

    /**
     * If anti-alias text is enabled, the value from RenderingHints to use for AA smoothing in Java2D. Defaults to
     * {@link java.awt.RenderingHints#VALUE_TEXT_ANTIALIAS_ON}.
     *
     * @return Current AA rendering hint
     */
    public Object getRenderingHints() {
        return antiAliasRenderingHint;
    }

    /**
     * If anti-alias text is enabled, the value from RenderingHints to use for AA smoothing in Java2D. Defaults to
     * {@link java.awt.RenderingHints#VALUE_TEXT_ANTIALIAS_ON}.
     *
     * @param renderingHints  rendering hint for AA smoothing in Java2D
     */
    public void setRenderingHints(Object renderingHints) {
        this.antiAliasRenderingHint = renderingHints;
    }

    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, String text) {
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        
        if (awtFont.getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        
        GlyphVector vector = awtFont.createGlyphVector(
                graphics.getFontRenderContext(),
                text);
        float[] result = vector.getGlyphPositions(0, text.length() + 1, null);
        
        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
        
        return result;
    }

    public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y) {
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        
        if (awtFont.getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        
        GlyphVector vector = ((AWTFSGlyphVector)fsGlyphVector).getGlyphVector();
        
        Rectangle result = vector.getGlyphPixelBounds(index, graphics.getFontRenderContext(), x, y);
        
        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
        
        return result;
    }

    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector) {
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        
        if (awtFont.getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        
        GlyphVector vector = ((AWTFSGlyphVector)fsGlyphVector).getGlyphVector();
        
        float[] result = vector.getGlyphPositions(0, vector.getNumGlyphs() + 1, null);
        
        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
        
        return result;
    }

    public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String text) {
        Object prevHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        
        if (awtFont.getSize() > threshold ) {
            prevHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        
        GlyphVector vector = awtFont.createGlyphVector(
                graphics.getFontRenderContext(),
                text);
        
        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, prevHint );
        }
        
        return new AWTFSGlyphVector(vector);
    }
}

