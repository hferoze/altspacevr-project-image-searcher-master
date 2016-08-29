package testsample.altvr.com.testsample.events;

import java.util.List;

import testsample.altvr.com.testsample.vo.PhotoVo;

/**
 * Created by hassan on 8/26/2016.
 */
public class SavedPhotosEvent {
    public List<PhotoVo> data;
    public SavedPhotosEvent(List<PhotoVo> data) { this.data = data; }
}
