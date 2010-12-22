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

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class FontInfo extends AllocationGuard 
{
	public class Glyph
	{
		char c;
		int x;
		int y;
		int w;
		int h;
		int x_offset;
		int y_offset;
		int x_advance;
	}

	public final static int INVALID_INDEX = -1;
	private Glyph[] mGlyphs = null;
	private Texture mTexture = null;
	private int mDrawableResID = -1;	// resource ID of the drawable file used for the font texture atlas
	private int mFontResID = -1;		// resource ID of the "foo.fnt" file (in the raw folder). Can be -1 for fixed width fonts loaded w/o a FNT file.
	private int mBase = 0;
	
    public void reset() 
    {
    	mGlyphs = null;
    	mTexture = null;
    	mDrawableResID = -1; // resource ID of the drawable file used for the font texture atlas
    	mFontResID = -1;
    	mBase = 0;
    }
    
	public void setup(int fontResID, int drawableResID)
	{
		mDrawableResID = drawableResID;
		mFontResID = fontResID;		
				
		mTexture = BaseObject.sSystemRegistry.longTermTextureLibrary.allocateTexture(drawableResID);

		if (null == mTexture)
			DebugLog.w("FONT", "Could not load font drawable: " + drawableResID);
		
		ContextParameters params = BaseObject.sSystemRegistry.contextParameters;
		InputStream input = params.context.getResources().openRawResource(fontResID);
		parseInputStream(input);
	}
	
	// Set up for a fixed with font texture. 
	public void setup(int drawableResID, int itemWidth, int itemHeight, char firstChar)
	{
		mDrawableResID = drawableResID;
		
		mTexture = BaseObject.sSystemRegistry.longTermTextureLibrary.allocateTexture(drawableResID);
		
		
		final int textureW = mTexture.width;
		//final int textureH = mTexture.mHeight;
		final int glyphW = itemWidth;
		final int glyphH = itemHeight;
		
		
		final int numChars = textureW / itemWidth;	// assumes 1 row for now... 
		
		mGlyphs = new Glyph[numChars];
		
		for ( int i = 0; i < numChars; i++)
		{
			Glyph g = new Glyph();
			mGlyphs[i] = g;
			
			final char c = (char)(((int)firstChar) + i);
			final int x = glyphW * i;
			final int y = 0;
			g.c = c;
			g.x = x;
			g.y = y;
			g.w = glyphW;
			g.h = glyphH;
			g.x_advance = glyphW;
		}
		
		//dumpFont();
	}
	
	public void dumpFont()
	{
		ContextParameters params = BaseObject.sSystemRegistry.contextParameters;
		if (-1 != mDrawableResID)
		{
			DebugLog.i("FONT","Font drawable res ID: " + mDrawableResID + ", " +				
					params.context.getResources().getResourceEntryName(mDrawableResID));
		}
		else
		{
			DebugLog.i("FONT","Font drawable res ID: " + mDrawableResID );
		}
		if (-1 != mFontResID)
		{
			DebugLog.i("FONT","Font FNT res ID: " + mFontResID + ", " +				
					params.context.getResources().getResourceEntryName(mFontResID));
		}
		else
		{
			DebugLog.i("FONT","Font FNT res ID: " + mFontResID );
		}
		
		for ( int i = 0; i < mGlyphs.length; i++)
		{
			Glyph g = mGlyphs[i];
			DebugLog.i("FONT", "[" + i + "] c: " + g.c );
			DebugLog.i("FONT", "[" + i + "] x: " + g.x );
			DebugLog.i("FONT", "[" + i + "] y: " + g.y );
			DebugLog.i("FONT", "[" + i + "] w: " + g.w );
			DebugLog.i("FONT", "[" + i + "] h: " + g.h );
			DebugLog.i("FONT", "[" + i + "] x_advance: " + g.x_advance );
		}
		
	}

	public final int getDrawableResID()
	{
		return mDrawableResID;
	}
	
	public final int getInfoResID()
	{
		return mFontResID;
	}
	
	public final int getBase()
	{
		return mBase;
	}
	
	public final Texture getTexture()
	{
		return mTexture;
	}
	
	public final int findGlyphIndex(char c)
	{
		final int length = mGlyphs.length;
		for (int i = 0; i < length; i++)
		{
			if (mGlyphs[i].c == c)
				return i;
		}
		
		return INVALID_INDEX;
	}
	
	public final Glyph getGlyphAtIndex(int index)
	{
		return mGlyphs[index];
	}
	
	public final Glyph findGlyph(char c)
	{
		final int length = mGlyphs.length;
		for (int i = 0; i < length; i++)
		{
			if (mGlyphs[i].c == c)
				return mGlyphs[i];
		}
		
		return null;
	}
	
	/*
	 * info face="Tahoma" size=24 bold=1 italic=0 charset="" unicode=1 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1 outline=0
	 * common lineHeight=24 base=20 scaleW=128 scaleH=128 pages=1 packed=0 alphaChnl=1 redChnl=0 greenChnl=0 blueChnl=0
	 * page id=0 file="generic_font_0.png"
	 * chars count=89
	 * char id=32   x=126   y=0     width=1     height=1     xoffset=0     yoffset=20    xadvance=6     page=0  chnl=15 
	 * char id=33   x=120   y=33    width=4     height=15    xoffset=2     yoffset=5     xadvance=7     page=0  chnl=15 
	 * char id=34   x=112   y=112   width=7     height=6     xoffset=1     yoffset=4     xadvance=10    page=0  chnl=15
	 * etc etc 
	 */
	private void parseInputStream(InputStream input)
	{
		// assume an 16kb buffer for reading in .FNT files.
		// we could alloc this once and re-use the buffer for
		// subsequent parsing... 
		StringBuilder builder = new StringBuilder( 1024 * 16 );	 
		
        try 
        {
			int read = input.read();
			while(read != -1)
			{
				builder.append((char)read);
				read = input.read();
			}
		} 
        catch (IOException e) 
		{
        	DebugLog.e("GarageFontParser", "parseInputStream: ", e);
			//e.printStackTrace();
		}
        
        String text = builder.toString();	
        String[] lines = text.split("\n");
        
        int glyphCount = 0;
        for ( int i = 0; i < lines.length; i++ )
        {
        	String line = lines[i];
        	
        	if (line.contains("char id="))
        		glyphCount++;
        	
        	if (line.contains("base="))
        	{
        		mBase = parseInt(line, "base=");
        	}
        }
		
        mGlyphs = new Glyph[glyphCount];
        glyphCount = 0;
        for ( int i = 0; i < lines.length; i++ )
        {
        	if (lines[i].contains("char id="))
        	{
        		mGlyphs[glyphCount] = parseGlyph(lines[i]);
        		glyphCount++;
        	}
        }
	}
	
	private Glyph parseGlyph(String s)
	{
		// char id=42   x=2     y=6     width=12     height=21     xoffset=0     yoffset=0    xadvance=12     page=0  chnl=0
		Glyph g = new Glyph();
			
		final int id = parseInt(s, "char id=");
		g.c = (char)id;
		g.x = parseInt(s, "x=");
		g.y = parseInt(s, "y=");
		g.w = parseInt(s, "width=");
		g.h = parseInt(s, "height=");
		g.x_offset = parseInt(s, "xoffset=");
		g.y_offset = parseInt(s, "yoffset=");
		g.x_advance = parseInt(s, "xadvance=");
		
		return g;
	}
	
	private int parseInt(String s, String sub)
	{
		final int start = s.indexOf(sub) + sub.length();
		final int end = Math.min(start+3, s.length());
		int i = (int)'\0';
		
        try 
        {
        	i = Integer.parseInt(s.substring(start, end).trim());
		} 
        catch (NumberFormatException e) 
		{
        	DebugLog.e("GarageFontParser", "parseInt: ", e);
			//e.printStackTrace();
		}
		
		return i;
	}
}
