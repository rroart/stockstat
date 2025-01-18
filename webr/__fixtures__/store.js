import { configureStore } from '@reduxjs/toolkit'

export const getStore = (obj = {}) => configureStore((state) => state, obj)
