package com.dbstar.guodian.model;

import android.os.StatFs;

import com.dbstar.guodian.util.StringUtil;

public class GDDiskInfo {

	static public class DiskInfo {
		public long RawDiskSize;
		public long RawDiskSpace;

		public String DiskSize;
		public String DiskSpace;
	}

	static public DiskInfo getDiskInfo(String diskPath, boolean convert) {
		StatFs sf = new StatFs(diskPath);
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();

		DiskInfo info = new DiskInfo();

		long diskSize = blockSize * blockCount;
		info.RawDiskSize = diskSize;

		long diskSpace = blockSize * availCount;
		info.RawDiskSpace = diskSpace;

		if (convert) {

			StringUtil.SizePair diskSizePair = StringUtil.formatSize(diskSize);
			info.DiskSize = StringUtil.formatFloatValue(diskSizePair.Value)
					+ StringUtil.getUnitString(diskSizePair.Unit);

			StringUtil.SizePair diskSpacePair = StringUtil
					.formatSize(diskSpace);
			info.DiskSpace = StringUtil.formatFloatValue(diskSpacePair.Value)
					+ StringUtil.getUnitString(diskSpacePair.Unit);
		}

		return info;
	}
}
