/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.replica.replicaisland;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11Ext;

public class DrawableText extends DrawableObject
{
	protected Texture mTexture = null;
	protected FontInfo mFont = null;
	
	
	/* position not stored with drawable
	protected float mX = 0;
	protected float mY = 0;
	*/
	
	protected int mW = 0;
	protected int mH = 0;
	protected int[] mCrop = new int[4];
	
	protected float mOpacity = 1.0f;
	protected float mRed 	= 1.0f;
	protected float mGreen 	= 1.0f;
	protected float mBlue 	= 1.0f;
	
	
	protected int mMaxGlyphs = 0;
	protected FontInfo.Glyph mGlyphs[] = null;
	protected StringBuilder mBuilder = null;
	
	protected boolean mGlyphsDirty = false;
	protected int mLength = 0;
	
	
	protected FontInfo.Glyph mHeader = null;
	
	DrawableText(int maxCharacters) 
    {
        super();
        mMaxGlyphs = maxCharacters;
		mGlyphs = new FontInfo.Glyph[maxCharacters];
		mBuilder = new StringBuilder(maxCharacters);
    }
	
	public void setup(int fontResID, int drawableResID)
	{
		mFont = BaseObject.sSystemRegistry.fontLibrary.findOrLoadFont(fontResID, drawableResID);
		mTexture = mFont.getTexture();		
		mLength = 0;
	}
	
	public void setup(int drawableResID)
	{
		mFont = BaseObject.sSystemRegistry.fontLibrary.findFont(drawableResID);
		mTexture = mFont.getTexture();
		mLength = 0;
	}
	
	public void setup(FontInfo font)
	{
		mFont = font;
		mTexture = font.getTexture();
		mLength = 0;
	}

	
	public final void setColor(float r, float g, float b)
	{
		mRed = r;
		mGreen = g;
		mBlue = b;
	}
	
	public final void setOpacity(float o)
	{
		mOpacity = o;
	}
	
	/* position not stored with drawable	 
	public final void setPosition(float x, float y)
	{
		mX = x;
		mY = y;
	}
	public final void setX(float x)
	{
		mX = x;
	}	
	public final float getX()
	{
		return mX;
	}
	
	public final void setY(float y)
	{
		mY = y;
	}
	
	public final float getY()
	{
		return mY;
	}
	*/
	
		
	public final Texture getTexture()
	{
		return mTexture;
	}

	public final void clearText()
	{
		mBuilder.setLength(0);
		mLength = 0;
		mGlyphsDirty = true;
	}
	
	public final int getLength()
	{
		return mLength;
	}
		
	public final void setFloat(float f)
	{
		mBuilder.setLength(0);
		mBuilder.append(f);
		mGlyphsDirty = true;
	}
	
	public final void setInt(int i)
	{
		// TODO: OPTOMIZE: we can skip the builder step here
		// See HudSystem - intToDigitArray
		mBuilder.setLength(0);
		mBuilder.append(i);
		mGlyphsDirty = true;
	}
	
	public final void setString(String s)
	{
		// TODO: OPTOMIZE: we can skip the builder step here
		mBuilder.setLength(0);
		mBuilder.append(s);
		mGlyphsDirty = true;		
	}
	
	public final void setHeader(char c)
	{
		mGlyphsDirty = true;
		mHeader = mFont.findGlyph(c);
	}
	
	public final int getWidth()	// in pixels.  
	{
		refreshGlyphs();
		return mW;
	}
	
	public final int getHeight()// in pixels.  
	{
		refreshGlyphs();
		return mH;
	}
	
	
	private void refreshGlyphs()
	{
		if (!mGlyphsDirty)
			return;
		
		mLength = 0;
		
		if (null != mHeader)
		{
			mLength = 1;
			mGlyphs[0] = mHeader;
		}
		
		final int length = mBuilder.length();
		for ( int i = 0; i < length && i < mMaxGlyphs; i++ )
		{
			final char c = mBuilder.charAt(i);			
			mGlyphs[mLength] = mFont.findGlyph(c);
			++mLength;
		}
		
		mW = 0;
		mH = 0;
		
		
		//String s = "";
		
		FontInfo.Glyph g = null;
		for ( int i = 0; i < mLength; i++ )
		{
			g = mGlyphs[i];
			if ( g == null )
				continue;
			
			mW += g.x_advance;
			mH = Math.max(mH, g.h);
			
			//s += g.c;
		}
		
		//GarageDebug.LogInfo("refreshGlyphs", s + ", w = " + mW + ", h = " + mH);
		
		mGlyphsDirty = false;
	}

	@Override
	public void draw(float mX, float mY, float scaleX, float scaleY) 
	{
		
        final float opacity = mOpacity;        
        if ( opacity <= 0 )
        	return;
		
        refreshGlyphs();
        final int length = getLength();
        if ( length <= 0 )
        	return;        

        final GL10 gl = OpenGLSystem.getGL();
        final Texture texture = mTexture;
        
        if (gl == null || texture == null || !texture.loaded )
        	return;
            
        
        final boolean changeColor = 
        	opacity < 1 || mRed < 1 || mGreen < 1 || mBlue < 1;
        	
    	
        if ( changeColor )
        {
        	gl.glColor4f(mRed * opacity, mGreen * opacity, mBlue * opacity, opacity);
        }
        
        
		final int base = mFont.getBase();

		
        float x, y, w, h;
        float x_advance = 0;
        FontInfo.Glyph g = null; 
        
        
        OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.name);
		for ( int i = 0; i < length; i++ )
		{
			g = mGlyphs[i];
			
			if ( null == g )
			{
				//x_advance += x_advance / (i+1);	// I think this is right, need to test... 
				//continue;
				break;
			}
			
			x = mX + x_advance + g.x_offset;
			y = mY + (base-(g.h+g.y_offset));
			w = g.w;
			h = g.h;
			
			x_advance += g.x_advance;
			
			final int left = g.x;
			final int bottom = g.y + g.h;
			final int width = g.w;
			final int height = g.h;
			
	        mCrop[0] = left;
	        mCrop[1] = bottom;
	        mCrop[2] = width;
	        mCrop[3] = -height;
			
	        OpenGLSystem.setTextureCrop(mCrop);
			((GL11Ext) gl).glDrawTexfOES(x, y, 0, w, h);
		}
		
		

		if (changeColor)
		{
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
		
	}



}
