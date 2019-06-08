package cho.nico.com.test;

import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;

public class FacePreviewInfo {
    private FaceInfo faceInfo;
    private LivenessInfo livenessInfo;
    private int trackId;
    private int gender,age;

    public FacePreviewInfo(FaceInfo faceInfo, LivenessInfo livenessInfo, int trackId, int gender, int age) {
        this.faceInfo = faceInfo;
        this.livenessInfo = livenessInfo;
        this.trackId = trackId;
        this.gender = gender;
        this.age = age;
    }

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }

    public LivenessInfo getLivenessInfo() {
        return livenessInfo;
    }

    public void setLivenessInfo(LivenessInfo livenessInfo) {
        this.livenessInfo = livenessInfo;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }


    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
