import { useRouter } from "expo-router";
import React from 'react';
import AttendanceTrackingScreen from "../attendanceTrackingT";
import { useDate } from "~/hooks";
import { AuthWrapper } from "~/components";


export default function AttendanceTrackingScreenWrapper() {
    const router = useRouter();
    const realTimeDate = useDate()

    return  <AttendanceTrackingScreen />;

};




