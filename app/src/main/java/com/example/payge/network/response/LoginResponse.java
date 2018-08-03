package com.example.payge.network.response;

public class LoginResponse {

    /**
     * code : 00000
     * data : {"facePic":"http://10.101.70.246:8888/group1/M00/50/B1/CmVG9ltFzQ2APQDcAADJRBYg4oI007.jpg","name":"上海物管","token":"eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJib3NzMDEwMSIsImV4cCI6MTU2NDU0MDE4NX0.aaN-KwBZ-LicgM-WQ-tDG_5fyChPnhuWZgGaSw9Y_OnsG1g1XmhONXJJVMbvQnpj_f62_5PrR3nmm-8vaMMxPPH4zpaR5KIf8fxcvqqSF1xxiIyj1DIUWFZLzCX-w-G1jmAYu_-NgR01QJfe-q9stq-YIfUUHWR51O7la5R_zKc","userId":"b9e3a264276448369e60e899b04a8ee9","userName":"boss0101"}
     * message : success
     */

    public String code;
    public Data data;
    public String message;

    public static class Data {
        /**
         * facePic : http://10.101.70.246:8888/group1/M00/50/B1/CmVG9ltFzQ2APQDcAADJRBYg4oI007.jpg
         * name : 上海物管
         * token : eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJib3NzMDEwMSIsImV4cCI6MTU2NDU0MDE4NX0.aaN-KwBZ-LicgM-WQ-tDG_5fyChPnhuWZgGaSw9Y_OnsG1g1XmhONXJJVMbvQnpj_f62_5PrR3nmm-8vaMMxPPH4zpaR5KIf8fxcvqqSF1xxiIyj1DIUWFZLzCX-w-G1jmAYu_-NgR01QJfe-q9stq-YIfUUHWR51O7la5R_zKc
         * userId : b9e3a264276448369e60e899b04a8ee9
         * userName : boss0101
         */

        public String facePic;
        public String name;
        public String token;
        public String userId;
        public String userName;
    }
}
