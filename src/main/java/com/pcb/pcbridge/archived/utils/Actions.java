package com.pcb.pcbridge.archived.utils;

public class Actions
{
	public interface Action0 {
		void Call();
	}

	public interface Action1<A> {
		void Call(A arg1);
	}
	
	public interface Action2<A, B> {
		void Call(A arg1, B arg2);
	}
	
	public interface Action3<A, B, C> {
		void Call(A arg1, B arg2, C arg3);
	}
}

