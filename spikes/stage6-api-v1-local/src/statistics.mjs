import {invalid} from '../../stage6-auth-local/src/errors.mjs';

const ZONE='Europe/Prague';
const parts=new Intl.DateTimeFormat('en-CA',{
 timeZone:ZONE,year:'numeric',month:'2-digit',day:'2-digit',
 hour:'2-digit',minute:'2-digit',second:'2-digit',hourCycle:'h23'
});

function civil(value){
 if(typeof value!=='string'||!/^\d{4}-\d{2}-\d{2}$/.test(value))throw invalid();
 const [year,month,day]=value.split('-').map(Number);
 const candidate=Date.UTC(year,month-1,day);
 if(!Number.isFinite(candidate)||new Date(candidate).toISOString().slice(0,10)!==value)throw invalid();
 return {year,month,day,candidate};
}
function localParts(epoch){
 const values=Object.fromEntries(parts.formatToParts(new Date(epoch)).filter(x=>x.type!=='literal').map(x=>[x.type,Number(x.value)]));
 return Date.UTC(values.year,values.month-1,values.day,values.hour,values.minute,values.second);
}
function midnight(value){
 const {candidate}=civil(value);
 const epoch=candidate-(localParts(candidate)-candidate);
 const rendered=parts.formatToParts(new Date(epoch)).filter(x=>x.type!=='literal').map(x=>x.value);
 if(`${rendered[0]}-${rendered[1]}-${rendered[2]} ${rendered[3]}:${rendered[4]}:${rendered[5]}`!==`${value} 00:00:00`)throw invalid();
 return epoch;
}
function next(value){
 const {year,month,day}=civil(value);
 return new Date(Date.UTC(year,month-1,day+1)).toISOString().slice(0,10);
}
const wire=epoch=>new Date(epoch).toISOString().replace('.000Z','.000000Z');

export function pragueWindow(civilDate){
 const start=midnight(civilDate),end=midnight(next(civilDate));
 return {
  timeZone:ZONE,
  startInclusive:wire(start),
  endExclusive:wire(end),
  elapsedHours:(end-start)/3600000
 };
}

export function literalSearch(text,query){
 if(typeof text!=='string'||typeof query!=='string'||!query.length) return false;
 return text.normalize('NFC').toLocaleLowerCase('und').includes(query.normalize('NFC').toLocaleLowerCase('und'));
}
