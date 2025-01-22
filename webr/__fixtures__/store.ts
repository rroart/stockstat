import { configureStore } from '@reduxjs/toolkit'
import { createStore } from 'redux'
export const getStoreOld = (obj = {}) => createStore((state) => state, obj)
/*
export const store = configureStore({
    reducer: {},
})

export const getStore = store;
*/
//export const getStoreOld = (obj = {}) => configureStore((state) => state, obj)
