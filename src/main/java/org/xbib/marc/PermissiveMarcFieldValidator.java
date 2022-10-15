package org.xbib.marc;

public class PermissiveMarcFieldValidator implements MarcFieldValidator {

    public PermissiveMarcFieldValidator() {
    }

    @Override
    public String validateTag(String tagCandidate) {
        return tagCandidate;
    }

    @Override
    public String validateIndicator(String indicatorCandidate) {
        return indicatorCandidate;
    }

    @Override
    public String validateSubfieldId(String subfieldIdCandidate) {
        return subfieldIdCandidate;
    }

    @Override
    public boolean isTagValid(String tag) {
        return true;
    }

    @Override
    public boolean isIndicatorValid(String indicator) {
        return true;
    }

    @Override
    public boolean isSubfieldIdValid(String subfieldId) {
        return true;
    }
}
