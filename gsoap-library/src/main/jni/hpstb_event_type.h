#ifndef _hpstb_event_type_h_
#define _hpstb_event_type_h_

typedef enum
{
    HPSTBEvent_ArriveEPG,//7days EPG
    HPSTBEvent_ArrivePF,//present/follow
    HPSTBEvent_CurrentPlayingOnTV,//channel on TV is changed
    HPSTBEvent_LostConnection,//lost connetction to STB
    HPSTBEvent_MasterRoleRequest,//another slave client request for master role
    HPSTBEvent_BecomeMaster,//a slave client becomes a master
    HPSTBEvent_BecomeSlave,//a master client becomes a slave
    HPSTBEvent_TunerLocked,
    HPSTBEvent_TunerUnlock,
    HPSTBEvent_CA_ShowMessage,
    HPSTBEvent_CA_HideMessage,
    HPSTBEvent_CA_CardOut,
    HPSTBEvent_CA_CardIn,
    HPSTBEvent_ArriveSSDP,
    HPSTBEvent_NEED_PARENTRATING_PASSWD
}HPSTBEvent_e;

#endif