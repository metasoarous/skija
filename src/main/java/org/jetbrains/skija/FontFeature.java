package org.jetbrains.skija;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Tolerate;

import java.util.Objects;
import java.util.regex.*;

@AllArgsConstructor
@EqualsAndHashCode
public class FontFeature {
    public final int _tag;
    @Getter
    public final int _value;
    @Getter
    public final long _start;
    @Getter
    public final long _end;

    public static final long GLOBAL_START = 0;
    public static final long GLOBAL_END = Long.MAX_VALUE;
    public static final FontFeature[] EMPTY = new FontFeature[0];

    public static int tag(String name) {
        assert name.length() == 4 : "Name must be exactly 4 symbols, got: '" + name + "'";
        return (name.charAt(0) & 0xFF) << 24
             | (name.charAt(1) & 0xFF) << 16
             | (name.charAt(2) & 0xFF) << 8
             | (name.charAt(3) & 0xFF);
    }

    public static String untag(int tag) {
        return new String(new byte[] { (byte) (tag >> 24 & 0xFF),
                                       (byte) (tag >> 16 & 0xFF),
                                       (byte) (tag >> 8 & 0xFF),
                                       (byte) (tag & 0xFF) });
    }

    public FontFeature(String feature, int value, long start, long end) {
        this(tag(feature), value, start, end);
    }

    public FontFeature(String feature, int value) {
        this(tag(feature), value, GLOBAL_START, GLOBAL_END);
    }

    public FontFeature(String feature, boolean value) {
        this(tag(feature), value ? 1 : 0, GLOBAL_START, GLOBAL_END);
    }

    public FontFeature(String feature) {
        this(tag(feature), 1, GLOBAL_START, GLOBAL_END);
    }

    public String getTag() {
        return untag(_tag);
    }

    @Override
    public String toString() {
        String range = "";
        if (_start > 0 || _end < Long.MAX_VALUE) {
            range = "[" + (_start > 0 ? _start : "") + ":" + (_end < Long.MAX_VALUE ? _end : "") + "]";
        }
        String valuePrefix = "";
        String valueSuffix = "";
        if (_value == 0)
            valuePrefix = "-";
        else if (_value == 1)
            valuePrefix = "+";
        else
            valueSuffix = "=" + _value;
        return "FontFeature(" + valuePrefix + getTag() + range + valueSuffix + ")";
    }

    public static Pattern _splitPattern = Pattern.compile("\\s+");
    public static Pattern _featurePattern = Pattern.compile("(?<sign>[-+])?(?<tag>[a-z0-9]{4})(?:\\[(?<start>\\d+)?:(?<end>\\d+)?\\])?(?:=(?<value>\\d+))?");

    public static FontFeature parseOne(String s) {
        Matcher m = _featurePattern.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("Can’t parse FontFeature: " + s);
        int value = m.group("value") != null ? Integer.parseInt(m.group("value"))
                    : m.group("sign") == null ? 1
                    : "-".equals(m.group("sign")) ? 0
                    : 1;
        long start = m.group("start") == null ? 0 : Long.parseLong(m.group("start"));
        long end = m.group("end") == null ? Long.MAX_VALUE : Long.parseLong(m.group("end"));
        return new FontFeature(m.group("tag"), value, start, end);
    }

    public static FontFeature[] parse(String s) {
        return _splitPattern.splitAsStream​(s).map(FontFeature::parseOne).toArray(FontFeature[]::new);
    }
}