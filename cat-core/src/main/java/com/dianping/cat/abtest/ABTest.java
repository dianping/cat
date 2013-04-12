package com.dianping.cat.abtest;

public interface ABTest {

	public int getTestId();

	public boolean isDefaultGroup();

	public boolean isGroupA();

	public boolean isGroupB();

	public boolean isGroupC();

	public boolean isGroupD();

	public boolean isGroupE();

	public boolean isGroup(String name);

	/** A default ABTest which id is 0 and group is default */
	public static final ABTest DEFAULT = new ABTest() {

		@Override
		public boolean isGroupE() {
			return false;
		}

		@Override
		public boolean isGroupD() {
			return false;
		}

		@Override
		public boolean isGroupC() {
			return false;
		}

		@Override
		public boolean isGroupB() {
			return false;
		}

		@Override
		public boolean isGroupA() {
			return false;
		}

		@Override
		public boolean isGroup(String name) {
			return false;
		}

		@Override
		public boolean isDefaultGroup() {
			return true;
		}

		@Override
		public int getTestId() {
			return 0;
		}
	};

}
