/*
 ** 2011 September 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.struct;

import info.ata4.bsplib.lump.LumpDataInput;
import info.ata4.bsplib.lump.LumpDataOutput;
import java.io.IOException;

/**
 * Generic interface for classes that emulate C/C++ structures.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface DStruct {
    
    public int getSize();
    public void read(LumpDataInput li) throws IOException;
    public void write(LumpDataOutput lo) throws IOException;
}
