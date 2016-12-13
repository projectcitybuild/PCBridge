package com.pcb.pcbridge.utils.cache;

public interface ICache<K, V> {
		
	V Get(K key);
	boolean ContainsKey(K key);
	void Forget(K key);
	void Clear();
	void Destroy();
	int Size();
	
}
