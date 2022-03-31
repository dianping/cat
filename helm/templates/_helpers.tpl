{{- define "cat.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cat.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{- define "cat.module.name" -}}
{{- .Values.moduleName | default .Release.Name }}
{{- end }}

{{- define "cat.mysql.name" -}}
{{ include "cat.module.name" . }}-mysql
{{- end }}

{{- define "cat.server.name" -}}
{{ include "cat.module.name" . }}-server
{{- end }}

{{- define "cat.module.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cat.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "cat.module.labels" -}}
helm.sh/chart: {{ include "cat.chart" . }}
{{ include "cat.module.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "cat.mysql.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cat.mysql.name" . }}
app.kubernete.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "cat.server.selectorLabels" -}}
app.kubernetes.io/name: {{ include "cat.server.name" . }}
app.kubernete.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "cat.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "cat.module.name" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}


{{- define "cat.datasources-xml" -}}
<data-sources>  
  <data-source id="cat">
    <maximum-pool-size>{{ .Values.databases.single.writable.cat.maximumPoolSize }}</maximum-pool-size>
    <connection-timeout>1s</connection-timeout>
    <idle-timeout>10m</idle-timeout>
    <statement-cache-size>1000</statement-cache-size>
    <properties>
      <driver>com.mysql.jdbc.Driver</driver>
      <url><![CDATA[jdbc:mysql://{{ .Values.databases.single.writable.cat.host }}:{{ .Values.databases.single.writable.cat.port }}/{{ .Values.databases.single.writable.cat.dbname }}]]></url>
      <user>{{ .Values.databases.single.writable.cat.username }}</user>
      <password>{{ .Values.databases.single.writable.cat.password }}</password>
      <connectionProperties><![CDATA[useUnicode=true&autoReconnect=true]]></connectionProperties>
    </properties>
  </data-source>
</data-sources>
{{- end }}