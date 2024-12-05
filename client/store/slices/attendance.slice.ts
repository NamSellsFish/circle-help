import { createSlice } from "@reduxjs/toolkit";
import { useAppSelector } from "~/hooks";

export interface attendanceState {
    type: 'PunchIn' | 'PunchOut' | undefined;
    currDate: Date;
    punchInTime: string;
    punchOutTime: string;
    totalHours: string;
    imageUrl: string;
}

export function dateToStringFormat(date: Date): string {
    return date.toLocaleDateString('en-US', {
        weekday: 'long',
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    })
}

export function dateToDateOnlyString(date: Date): string {
    return date.toISOString().split('T')[0]
}

export function dateToTimeOnlyString(date: Date): string {
    return date.toISOString().split('T')[1].substring(0, 5)
}

const initialState: attendanceState = {
    type: undefined,
    currDate: new Date(),
    punchInTime: '--:--',
    punchOutTime: '--:--',
    totalHours: '--',
    imageUrl: ''
};

const attendanceSlice = createSlice({
    name: 'attendance',
    initialState,
    reducers: {
        updateAttendanceInfoItem: (state, action) => {
            Object.assign(state, action.payload)
        },
        resetAttendanceInfoItem: (state) => {
            Object.assign(state, initialState)
        },
    }
})



export const {
    updateAttendanceInfoItem,
    resetAttendanceInfoItem
} = attendanceSlice.actions


export default attendanceSlice.reducer
export function getAttendance(): attendanceState { 
    return useAppSelector((state) => state.attendance) 
}

export const initialAttendanceState = initialState