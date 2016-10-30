/*------------------------------------------------------------------------------
* rtcm.c : rtcm functions
*
*          Copyright (C) 2009-2014 by T.TAKASU, All rights reserved.
*
* references :
*     [1] RTCM Recommended Standards for Differential GNSS (Global Navigation
*         Satellite Systems) Service version 2.3, August 20, 2001
*     [2] RTCM Standard 10403.1 for Differential GNSS (Global Navigation
*         Satellite Systems) Services - Version 3, Octobar 27, 2006
*     [3] RTCM 10403.1-Amendment 3, Amendment 3 to RTCM Standard 10403.1
*     [4] RTCM Paper, April 12, 2010, Proposed SSR Messages for SV Orbit Clock,
*         Code Biases, URA
*     [5] RTCM Paper 012-2009-SC104-528, January 28, 2009 (previous ver of [4])
*     [6] RTCM Paper 012-2009-SC104-582, February 2, 2010 (previous ver of [4])
*     [7] RTCM Standard 10403.1 - Amendment 5, Differential GNSS (Global
*         Navigation Satellite Systems) Services - version 3, July 1, 2011
*     [8] RTCM Paper 019-2012-SC104-689 (draft Galileo ephmeris messages)
*     [9] RTCM Paper 163-2012-SC104-725 (draft QZSS ephemeris message)
*     [10] RTCM Paper 059-2011-SC104-635 (draft Galileo and QZSS ssr messages)
*     [11] RTCM Paper 034-2012-SC104-693 (draft multiple signal messages)
*     [12] RTCM Paper 133-2012-SC104-709 (draft QZSS MSM messages)
*     [13] RTCM Paper 122-2012-SC104-707.r1 (draft MSM messages)
*     [14] RTCM Standard 10403.2, Differential GNSS (Global Navigation Satellite
*          Systems) Services - version 3, February 1, 2013
*     [15] RTCM Standard 10403.2, Differential GNSS (Global Navigation Satellite
*          Systems) Services - version 3, with amendment 1/2, november 7, 2013
*     [16] Proposal of new RTCM SSR Messages (ssr_1_gal_qzss_sbas_dbs_v05)
*          2014/04/17
*
* version : $Revision:$ $Date:$
* history : 2009/04/10 1.0  new
*           2009/06/29 1.1  support type 1009-1012 to get synchronous-gnss-flag
*           2009/12/04 1.2  support type 1010,1012,1020
*           2010/07/15 1.3  support type 1057-1068 for ssr corrections
*                           support type 1007,1008,1033 for antenna info
*           2010/09/08 1.4  fix problem of ephemeris and ssr sequence upset
*                           (2.4.0_p8)
*           2012/05/11 1.5  comply with RTCM 3 final SSR format (RTCM 3
*                           Amendment 5) (ref [7]) (2.4.1_p6)
*           2012/05/14 1.6  separate rtcm2.c, rtcm3.c
*                           add options to select used codes for msm
*           2013/04/27 1.7  comply with rtcm 3.2 with amendment 1/2 (ref[15])
*           2013/12/06 1.8  support SBAS/BeiDou SSR messages (ref[16])
*-----------------------------------------------------------------------------*/
#include "rtklib.h"
#include <android/log.h>

static const char rcsid[]="$Id:$";

/* function prototypes -------------------------------------------------------*/
extern int decode_rtcm2(rtcm_t *rtcm);

/* constants -----------------------------------------------------------------*/

#define RTCM2PREAMB 0x66        /* rtcm ver.2 frame preamble */

/* initialize rtcm control -----------------------------------------------------
* initialize rtcm control struct and reallocate memory for observation and
* ephemeris buffer in rtcm control struct
* args   : rtcm_t *raw   IO     rtcm control struct
* return : status (1:ok,0:memory allocation error)
*-----------------------------------------------------------------------------*/
extern int init_rtcm(rtcm_t *rtcm)
{
    gtime_t time0={0};
    obsd_t data0={{0}};
    eph_t  eph0 ={0,-1,-1};
    geph_t geph0={0,-1};
    ssr_t ssr0={{{0}}};
    int i,j;
    
    trace(3,"init_rtcm:\n");
    
    rtcm->staid=rtcm->stah=rtcm->seqno=rtcm->outtype=0;
    rtcm->time=rtcm->time_s=time0;
    rtcm->sta.name[0]=rtcm->sta.marker[0]='\0';
    rtcm->sta.antdes[0]=rtcm->sta.antsno[0]='\0';
    rtcm->sta.rectype[0]=rtcm->sta.recver[0]=rtcm->sta.recsno[0]='\0';
    rtcm->sta.antsetup=rtcm->sta.itrf=rtcm->sta.deltype=0;
    for (i=0;i<3;i++) {
        rtcm->sta.pos[i]=rtcm->sta.del[i]=0.0;
    }
    rtcm->sta.hgt=0.0;
    rtcm->dgps=NULL;
    for (i=0;i<MAXSAT;i++) {
        rtcm->ssr[i]=ssr0;
    }
    rtcm->msg[0]=rtcm->msgtype[0]=rtcm->opt[0]='\0';
    for (i=0;i<6;i++) rtcm->msmtype[i][0]='\0';
    rtcm->obsflag=rtcm->ephsat=0;
    for (i=0;i<MAXSAT;i++) for (j=0;j<NFREQ+NEXOBS;j++) {
        rtcm->cp[i][j]=0.0;
        rtcm->lock[i][j]=rtcm->loss[i][j]=0;
        rtcm->lltime[i][j]=time0;
    }
    rtcm->nbyte=rtcm->nbit=rtcm->len=0;
    rtcm->word=0;
    for (i=0;i<100;i++) rtcm->nmsg2[i]=0;
    for (i=0;i<300;i++) rtcm->nmsg3[i]=0;
    
    rtcm->obs.data=NULL;
    rtcm->nav.eph =NULL;
    rtcm->nav.geph=NULL;
    
    /* reallocate memory for observation and ephemris buffer */
    if (!(rtcm->obs.data=(obsd_t *)malloc(sizeof(obsd_t)*MAXOBS))||
        !(rtcm->nav.eph =(eph_t  *)malloc(sizeof(eph_t )*MAXSAT))||
        !(rtcm->nav.geph=(geph_t *)malloc(sizeof(geph_t)*MAXPRNGLO))) {
        free_rtcm(rtcm);
        return 0;
    }
    rtcm->obs.n=0;
    rtcm->nav.n=MAXSAT;
    rtcm->nav.ng=MAXPRNGLO;
    for (i=0;i<MAXOBS   ;i++) rtcm->obs.data[i]=data0;
    for (i=0;i<MAXSAT   ;i++) rtcm->nav.eph [i]=eph0;
    for (i=0;i<MAXPRNGLO;i++) rtcm->nav.geph[i]=geph0;
    return 1;
}
/* free rtcm control ----------------------------------------------------------
* free observation and ephemris buffer in rtcm control struct
* args   : rtcm_t *raw   IO     rtcm control struct
* return : none
*-----------------------------------------------------------------------------*/
extern void free_rtcm(rtcm_t *rtcm)
{
    trace(3,"free_rtcm:\n");
    
    /* free memory for observation and ephemeris buffer */
    free(rtcm->obs.data); rtcm->obs.data=NULL; rtcm->obs.n=0;
    free(rtcm->nav.eph ); rtcm->nav.eph =NULL; rtcm->nav.n=0;
    free(rtcm->nav.geph); rtcm->nav.geph=NULL; rtcm->nav.ng=0;
}
/* input rtcm 2 message from stream --------------------------------------------
* fetch next rtcm 2 message and input a message from byte stream
* args   : rtcm_t *rtcm IO   rtcm control struct
*          unsigned char data I stream data (1 byte)
* return : status (-1: error message, 0: no message, 1: input observation data,
*                  2: input ephemeris, 5: input station pos/ant parameters,
*                  6: input time parameter, 7: input dgps corrections,
*                  9: input special message)
* notes  : before firstly calling the function, time in rtcm control struct has
*          to be set to the approximate time within 1/2 hour in order to resolve
*          ambiguity of time in rtcm messages.
*          supported msgs RTCM ver.2: 1,3,9,14,16,17,18,19,22
*          refer [1] for RTCM ver.2
*-----------------------------------------------------------------------------*/
extern int input_rtcm2(rtcm_t *rtcm, unsigned char data)
{
    unsigned char preamb;
    int i;
    
    trace(5,"input_rtcm2: data=%02x\n",data);
    
    if ((data&0xC0)!=0x40) return 0; /* ignore if upper 2bit != 01 */
    
    for (i=0;i<6;i++,data>>=1) { /* decode 6-of-8 form */
        rtcm->word=(rtcm->word<<1)+(data&1);
        
        /* synchronize frame */
        if (rtcm->nbyte==0) {
            preamb=(unsigned char)(rtcm->word>>22);
            if (rtcm->word&0x40000000) preamb^=0xFF; /* decode preamble */
            if (preamb!=RTCM2PREAMB) continue;
            
            /* check parity */
            if (!decode_word(rtcm->word,rtcm->buff)) continue;
            rtcm->nbyte=3; rtcm->nbit=0;
            continue;
        }
        if (++rtcm->nbit<30) continue; else rtcm->nbit=0;
        
        /* check parity */
        if (!decode_word(rtcm->word,rtcm->buff+rtcm->nbyte)) {
            trace(2,"rtcm2 partity error: i=%d word=%08x\n",i,rtcm->word);
            rtcm->nbyte=0; rtcm->word&=0x3;
            continue;
        }
        rtcm->nbyte+=3;
        if (rtcm->nbyte==6) rtcm->len=(rtcm->buff[5]>>3)*3+6;
        if (rtcm->nbyte<rtcm->len) continue;
        rtcm->nbyte=0; rtcm->word&=0x3;
        
        /* decode rtcm2 message */
        decode_rtcm2(rtcm);
//        return decode_rtcm2(rtcm);
    }
    return 0;
}
/* input rtcm 2 message from file ----------------------------------------------
* fetch next rtcm 2 message and input a messsage from file
* args   : rtcm_t *rtcm IO   rtcm control struct
*          FILE  *fp    I    file pointer
* return : status (-2: end of file, -1...10: same as above)
* notes  : same as above
*-----------------------------------------------------------------------------*/
extern int input_rtcm2f(rtcm_t *rtcm, FILE *fp)
{
    int i,data=0,ret;
    
    trace(4,"input_rtcm2f: data=%02x\n",data);

//    for (i=0;i<4096;i++) {
//        if ((data=fgetc(fp))==EOF) return -2;
//        if ((ret=input_rtcm2(rtcm,(unsigned char)data))) return ret;
//    }
    //檢查檔案是否結束
    while (!feof(fp)){
        data=fgetc(fp);
        if ((ret=input_rtcm2(rtcm,(unsigned char)data))) return ret;
    }

    return 0;
}

/* generate rtcm 2 message -----------------------------------------------------
* generate rtcm 2 message
* args   : rtcm_t *rtcm   IO rtcm control struct
*          int    type    I  message type
*          int    sync    I  sync flag (1:another message follows)
* return : status (1:ok,0:error)
*-----------------------------------------------------------------------------*/
extern int gen_rtcm2(rtcm_t *rtcm, int type, int sync)
{
    trace(4,"gen_rtcm2: type=%d sync=%d\n",type,sync);
    
    rtcm->nbit=rtcm->len=rtcm->nbyte=0;
    
    /* not yet implemented */
    
    return 0;
}
