package astava.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class InputStreamByteSource implements ByteSource {
    private InputStream inputStream;
    private ArrayList<Integer> cache;
    private boolean cachedAll;

    public InputStreamByteSource(InputStream inputStream) {
        this.inputStream = inputStream;
        cache = new ArrayList<>();
    }

    @Override
    public int get(int index) {
        if(!cachedAll) {
            while (cache.size() < index) {
                try {
                    if (inputStream.available() > 0)
                        cache.add(inputStream.read());
                    if (!cachedAll)
                        cachedAll = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(cachedAll && index >= cache.size())
            return -1;

        return cache.get(index);
    }
}
