/*
** 2011 April 5
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
import info.ata4.bsplib.vector.Vector3f;
import java.io.IOException;

/**
 * Displacement vertex data structure.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DDispVert implements DStruct {

    public Vector3f vector;
    public float dist;
    public float alpha;

    public int getSize() {
        return 20;
    }

    public void read(LumpDataInput li) throws IOException {
        vector = li.readVector3f();
        dist = li.readFloat();
        alpha = li.readFloat();
    }

    public void write(LumpDataOutput lo) throws IOException {
        lo.writeVector3f(vector);
        lo.writeFloat(dist);
        lo.writeFloat(alpha);
    }
}
