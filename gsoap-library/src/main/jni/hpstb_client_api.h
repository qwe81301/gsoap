#ifndef _hpstb_client_api_h_
#define _hpstb_client_api_h_
#include "hpstb_errorcode.h"
#include "hpstb_event_type.h"

/**
@brief hpstb_Init
Init hpstb module.

@param[in] pInitInfo  JSON: e.g. {"port": 20000}
@return 
refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_Init(const char * pInitInfo);

/**
@brief hpstb_Free
Free a memory ptr which is got from a hpstb api.

@param[in] ptr:  
*/

void hpstb_Free(char *ptr);

typedef void(*_EventCallBack)(int event,void* arg, int arg_len, void* user_param);
/**
 * 
 * @param[in] cb 
 * event: HPSTBEvent_e:callback event 
 * arg :callback data 
 * arg_len:callback_data length 
 * (int event, void *arg, int arg_len, void user_param)
 *                   
 * @param[in] user_param 
 * Reserved input parameter
 * 
 * @return HPSTBErrCode 
 */
HPSTBErrCode hpstb_SetEventNotify(_EventCallBack cb, void* user_param);

/**
@brief hpstb_SetNotifyEnable
enable/disable notify task.
@param[in] iEnable:  0/1 
@return refer to HPSTBErrCode 
*/
HPSTBErrCode hpstb_SetNotifyEnable(int iEnable);

/**
@brief hpstb_ConnectToSTB
Client connect to STB. If success, client will get a token,
A token is a session-binding string. 

@param[in] pClientInfo     e.g. 
                        {"userid":"admin","uuid":"4028b88154d1460d0154d224060e000d"}
                        userid is xmpp username,uuid is for http
                        request
@param[in] pServerInfo   e.g. 
      {"server":"192.168.1.100:8100"}
@param[out] ppResultInfo   output JSON: e.g. 
      {"userid":"admin","token":"8a234hsa432e","expires":60},
      should call hpstb_Free after use.
@return  refer to HPSTBErrCode
         HPSTB_TOO_MANY_CLIENTS--refuse because of too many client
*/
HPSTBErrCode hpstb_ConnectToSTB(const char *pClientInfo, const char *pServerInfo, char **ppResultInfo);

/**
@brief hpstb_Disconnect
Client disconnect to STB.

@param[in] pClientInfo     e.g. 
      {"userid":"admin","token":"8a234hsa432e"}
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_Disconnect(const char *pClientInfo);

/**
@brief hpstb_RequestBind
Client request bind to stb. If success, client will get STB xmpp JID

@param[in] pClientInfo    e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppResultInfo   e.g. 
                   {"jid":"11011603240000001aabbccdde@218.57.146.181"},
                   should call hpstb_Free after use.
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_RequestBind(const char *pClientInfo, char **ppResultInfo);

/**
@brief hpstb_HandoverMasterRole
Handover current client's master role to another client,
and of cause then itself falls back to a slave.
Only a master client can call this API successfully.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pAnotherClient     e.g. 
                     {"userid":"USER2","ip":"10.0.0.5"}
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_HandoverMasterRole(const char *pClientInfo, const char *pAnotherClient);


/**
@brief hpstb_GetMasterClient
Get current STB's master client.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppMasterInfo    e.g. {"userid":"USER2", 
                   "ip":"10.0.0.5"}, should call hpstb_Free
                   after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetMasterClient(const char *pClientInfo, char **ppMasterInfo);

/**
@brief hpstb_SetClientRole
Change current client's role. A slave-to-master change 
may fail if current role is slave. Master-to-slave always
success because master has the right to give up it's role.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pRoleInfo    e.g. {"role":"slave"}
value of role: master/slave

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_SetClientRole(const char *pClientInfo, char *pRoleInfo);

/**
@brief hpstb_GetClientRole
Get current client role info from STB.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppRoleInfo    e.g. {"role":"slave"}, 
should call hpstb_Free after use. value of role: master/slave 

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetClientRole(const char *pClientInfo, char **ppRoleInfo);

/**
@brief hpstb_GetSTBInfo
Get stb info.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppStbInfo         e.g. 
{"hwver":"1.00","swver":"1.00","sn":"LC000000001234567890","mac":"BC20BA123456",
 "devicename":"bcm7251s","smartcardno":"8531103988190404","operator":"beijinggehua"}
, should call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetSTBInfo(const char *pClientInfo, char **ppStbInfo);


/**
@brief hpstb_GetSTBCapacity
Get stb capacity.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppCapacity     e.g. 
{"tunernum":4,"transcode":[{"type":"h264","resolutions":["640*480","352*288","176*144"]}],
 "audiodecode":[{"type":"aac"},{"type":"ac3"},{"type":"mpeg2"},{"type":"mpeg1"}],
 "videodecode":[{"type":"h265"},{"type":"h264"},{"type":"mpeg2"},{"type":"mpeg1"}],
 "gatewaymanage":"flase"}
, should call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetSTBCapacity(const char *pClientInfo, char **ppCapacity);


/**
@brief hpstb_SetBulletScreenEnable
Set BulletScreen enable (only for controller).

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pEnable        e.g. {"enable":"true"} 
              enable:true/false
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_SetBulletScreenEnable(const char *pClientInfo, const char *pEnable);


/**
@brief hpstb_GetBulletScreenEnable
Get BulletScreen enable.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppEnable        e.g. {"enable":"true"} 
               enable:true/false, should call hpstb_Free after
               use.
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetBulletScreenEnable(const char *pClientInfo, char **ppEnable);


/**
@brief hpstb_SendText
Send text message to STB (only for controller).

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pTextInfo        e.g. {"text":"goog tv 
                     show!!!","style":"bold","size":"big","color":"white","x":0,"y":0}
                     style:normal/bold/italic
                     size:big/middle/small
                     color:white/red/blue/black/yellow....
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_SendText(const char *pClientInfo, const char *pTextInfo);


/**
@brief hpstb_SendVoice
Send voice message to STB (only for controller).

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pPath           e.g. "/data/voice.wav"
@param[in] pVoiceInfo    e.g. 
                 {"format":"wav","size":1000000,"mix":"true"}
                 format:wav size:100000(B) mix:true/false
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_SendVoice(const char *pClientInfo, const char *pPath, const char *pVoiceInfo);


/**
@brief hpstb_SendKey
Send keyevent to STB (only for controller).

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pKeyInfo      e.g. {"key":"UP"} 
               key:UP/DOWN/LEFT/RIGHT/CENTER/MENU/BACK/HOME/1/2/3/4/5/6/7/8/9/0/VOLUME_UP/VOLUME_DOWN/POWER/RED
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_SendKey(const char *pClientInfo, const char *pKeyInfo);


/**
@brief hpstb_GetSceenshot
Get screenshots of STB.

@param[in] pClientInfo     e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pOption       e.g. {"content":"video"} 
              content:all/video
@param[out] ppImageData          output adrress of image, 
                  should call hpstb_Free after use.
@param[out] ppImageInfo     e.g. {"fmt":"bmp", 
                  "size": 1234238, "res":"1920*1080"}, should
                  call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetSceenshot(const char *pClientInfo, const char *pOption, char **ppImageData, char **ppImageInfo);


/**
@brief hpstb_GetChannelList
Get channel list of STB.

@param[in] pClientInfo          e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppChannelList        e.g.
[
{"chno":1,"name":"ch1","tsid":1,"serviceid":111,"freq":195000,"tunerid":1,"isHide":"false","isLock":"false","isFavor":"false","isHD":"true","type":"tv","bat":"1"},
{"chno":2,"name":"ch2","tsid":1,"serviceid":112,"freq":195000,"tunerid":1,"isHide":"false","isLock":"false","isFavor":"false","isHD":"true","type":"tv","bat":"2"},
......
]
, should call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetChannelList(const char *pClientInfo, char **ppChannelList);

/**
@brief hpstb_GetChannelClassification
Get channel classification of STB.

@param[in] pClientInfo          e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppChannelList        e.g.
[
{"bat":1,"name":"CCTV"},
{"bat":1,"name":"HDTV"},
......
]
hpstb_GetChannelList will get channel bat,according to bat you can get classification
by hpstb_GetChannelClassification
, should call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetChannelClassification(const char *pClientInfo, char **ppChannelClass);


/**
@brief hpstb_GetBookingList
Get booking list from STB.

@param[in] pClientInfo          e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppBookingList       e.g.
[
{"name":"ch1","freq":195000,"tsid":1,"serviceid":111,"program":"prog1","starttime":"2016/01/11 18:00","endtime":"2016/01/11 19:00","duration":3600},
{"name":"ch2","freq":203000,"tsid":3,"serviceid":112,"program":"prog2","starttime":"2016/01/11 18:00","endtime":"2016/01/11 19:00","duration":3600},
......
]
, should call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetBookingList(const char *pClientInfo, char **ppBookingList);


/**
@brief hpstb_SetBookingList
Set booking list to STB.(only for controller)

@param[in] pClientInfo         e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pBookingList        e.g.
[
{"name":"ch1","freq":195000,"tsid":1,"serviceid":111,"program":"prog1","starttime":"2016/01/11 18:00","endtime":"2016/01/11 19:00","duration":3600},
{"name":"ch2","freq":203000,"tsid":3,"serviceid":112,"program":"prog2","starttime":"2016/01/11 18:00","endtime":"2016/01/11 19:00","duration":3600},
......
]
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_SetBookingList(const char *pClientInfo, char *pBookingList);


/**
@brief hpstb_RequestPF
Request PF of STB.

@param[in] pClientInfo      e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pRequestInfo   e.g. 
                   {"freq":195000,"tsid":1,"serviceid":111}
@param[out] ppResult    
 e.g.
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":"","pfType":"present"}
]
, should call hpstb_Free after use.
usually,pf will arrive async,by notify,and maybe twice notify to get whole PF
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":"","pfType":"present"}
]
pfType:present/follow
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_RequestPF(const char *pClientInfo, const char *pRequestInfo, char **ppResult);

/**
@brief hpstb_RequestEPG
Request EPG of STB.

@param[in] pClientInfo      e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pRequestInfo   e.g. 
                   {"freq":195000,"tsid":1,"serviceid":111}
@param[out] ppResult    
 e.g. 
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":""}
] 
, should call hpstb_Free after use.
usually,epg will arrive async,by notify
[
{"frequency":259000,"tsId":10,"serviceId":200,"duration":2700,"parentRating":0,"eventName":"test","startDateTime":"2016-06-03 13:58:00","endDateTime":"2016-06-03 14:43:00","content":"","languageLocal":"chi","eventNameLocal":"test","contentLocal":"","languageSecond":"","eventNameSecond":"","contentSecond":""}
]

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_RequestEPG(const char *pClientInfo, const char *pRequestInfo, char **ppResult);



/**
@brief hpstb_PlayChannelOnTV
Play a specific channel on TV.

@param[in] pClientInfo  e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pChannelInfo e.g. 
                   {"freq":195000,"tsid":1,"serviceid":111}
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_PlayChannelOnTV(const char *pClientInfo, const char *pChannelInfo);


/**
@brief hpstb_GetPlayingOnTV
Get current playing channel on TV

@param[in] pClientInfo  e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppChannelInfo  e.g. 
                    {"freq":195000,"tsid":1,"serviceid":111},
                    should call hpstb_Free after use.
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetPlayingOnTV(const char *pClientInfo, char **ppChannelInfo);


/**
@brief hpstb_GetSharingChannels
Get the channels which is sharing on STB.

@param[in] pClientInfo  e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppSharingList e.g.
[
{
"url":"http://109.163.0.5/streaming/1.ts", 
"channel":{"freq":195000,"tsid":1,"serviceid":111}
},
{
"url":"http://109.163.0.5/streaming/2.ts", 
"channel":{"freq":203000,"tsid":3,"serviceid":112}
},
]
, should call hpstb_Free after use.

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetSharingChannels(const char *pClientInfo, char **ppSharingList);



/**
@brief hpstb_GetShareable
To see what channels can the STB share for now.

@param[in] pClientInfo  e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[out] ppShareable  e.g.{"channel":"current"}, 
should call hpstb_Free after use. shareable.channel 
none/current/any none -- no channels can be shared 
current -- only the current playing channel on TV can be shared
any -- any channel can be shared for now

@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_GetShareable(const char *pClientInfo, char **ppShareable);


/**
@brief hpstb_RequestShareChannel
If STB is already sharing this channel, just return ok.
request maybe fails if stb resources are not enough.
Client can use this API to switch channel,STB will automatically
stop the last sharing channel for this client, and returns 
a new url if necessary(maybe use the last url).

@param[in] pClientInfo  e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pRequestInfo  e.g.
{
"channel":{"freq":195000,"tsid":1,"serviceid":111},
"codec":{"type":"default","res":"default"},
"protocol":{"type":"hls","port":"default"},
"encrypt":"false"
}

channel: required
codec: optional  type:default/h264  res:default/low/mid
codec.res: default/low/mid
default -- original resolution
low -- 720*576 resolution
mid -- 1280*720 resolution
protocol:type: hls/http_socket  port:default(no support now)
encrypt: true/false(no support now)

@param[out] ppSharingInfo  e.g.
{
"url":"http://109.163.0.5/streaming/112.ts",
"channel":{"freq":195000,"tsid":1,"serviceid": 111}
}
, should call hpstb_Free after use.
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_RequestShareChannel(const char *pClientInfo, const char *pRequestInfo, char **ppSharingInfo);



/**
@brief hpstb_StopSharingChannel
Stop sharing a channel means only this client will stop
sharing this channel, if there are still some clients 
attaching on this sharing, STB will not stop sharing,
or else STB will really stop sharing.

@param[in] pClientInfo  e.g. 
                  {"userid":"admin","token":"8a234hsa432e"}
@param[in] pChannelInfo  
                   e.g.{"freq":195000,"tsid":1,"serviceid":111}
@return refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_StopSharingChannel(const char *pClientInfo, const char *pChannelInfo);


/**
@brief: hpstb_StartSsdp
Client start ssdp to STB.

@no param,should call hpstb_StartSsdp after use.
usually,ssdp info will arrive async,by notify
[
{"deviceIp":"192.168.1.108","deviceName":"inspurSTB","gsoapPort":10000}
]
@return: refer to HPSTBErrCode
*/
HPSTBErrCode hpstb_StartSsdp(void);


/**
@brief hpstb_StopSsdp
Client stop ssdp.

@no param
@return HPSTB_OK--success
         HPSTB_FAIL--fail
*/
HPSTBErrCode hpstb_StopSsdp(void);

/**
@brief hpstb_Check_Password
Client check password by STB.

@param[in] pClientInfo    e.g.{"userid":"admin","token":"8a234hsa432e"}
@param[in] pPasswdInfo    e.g.{"parentRating":"1234"}
@return refer to HPSTBErrCode:  HPSTB_OK--check success
                                HPSTB_FAIL--check fail
*/
HPSTBErrCode hpstb_Check_Password(const char *pClientInfo, const char *pPasswdInfo);


#endif