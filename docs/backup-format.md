# Backup Format

This document describes the backup file format used by Bangumi Manager Reformed,
including its data constraints and version compatibility policy.

## File Format

- Backup files are UTF-8 encoded JSON objects and normally use the `.bmbackup` extension.
- Imports are limited to 10 MiB.
- Dates use ISO-8601 `LocalDate` strings in `yyyy-MM-dd` format.
- `exportedAt` uses an ISO-8601 `Instant` string.
- Nullable values are represented by JSON `null`, rather than by omitting their keys.

## Root Object

```json
{
  "formatVersion": 2,
  "exportedAt": "2026-08-18T08:00:00Z",
  "appVersion": "0.4.0-beta01",
  "bangumis": [],
  "schedules": [],
  "calendarSettings": {},
  "globalSettings": {}
}
```

| Field | JSON type | Description |
| --- | --- | --- |
| `formatVersion` | Number | Backup schema version. |
| `exportedAt` | String | Time at which the backup was created. |
| `appVersion` | String | Version of the app that created the backup. |
| `bangumis` | Array | Bangumi records. |
| `schedules` | Array | Broadcast schedule anchors. |
| `calendarSettings` | Object | Calendar-related settings. |
| `globalSettings` | Object | Application-wide settings. |

All root fields are required.

## Bangumi

| Field | JSON type | Constraints |
| --- | --- | --- |
| `bangumiId` | Number | Positive and unique. |
| `title` | String | Must not be blank. |
| `seasonYear` | Number | From `1` through `9999`. |
| `seasonMonth` | Number | One of `1`, `4`, `7`, or `10`. |
| `myScore` | Number or null | From `0` through `100` when present. |
| `firstBroadcastDate` | String | ISO-8601 date. |
| `totalEpisodes` | Number or null | Positive when present. |
| `latestWatchedEpisode` | Number | Non-negative and no greater than `totalEpisodes` when known. |
| `isActive` | Boolean | Whether the item is visible as active. |
| `lastModifiedAtMillis` | Number | Non-negative Unix timestamp in milliseconds. Used by version 2. |
| `expectedEndDate` | String or null | ISO-8601 date. See the note below. |

`expectedEndDate` remains in exported files for format continuity. During import,
the stored value is ignored and recalculated from `totalEpisodes` and the imported
Schedule anchors after validation.

Version 1 uses `lastBasicInfoModifiedAtMillis` instead of `lastModifiedAtMillis`.

## BangumiSchedule

| Field | JSON type | Constraints |
| --- | --- | --- |
| `bangumiId` | Number | Must reference an imported Bangumi. |
| `episodeId` | Number | Positive and unique within its Bangumi. |
| `broadcastDate` | String | ISO-8601 date. |

Every Bangumi must have an episode-1 Schedule whose `broadcastDate` equals the
Bangumi's `firstBroadcastDate`. Later anchors are not required to have increasing dates.

## CalendarSettings

| Field | JSON type |
| --- | --- |
| `calendarInactiveVisibility` | Number |
| `calendarFinishedEpisodeVisible` | Boolean |
| `calendarFinishedBangumiVisible` | Boolean |
| `calendarWeeksBeforeCurrent` | Number |
| `calendarWeeksAfterCurrent` | Number |
| `calendarWeeksPrefix` | Number |

All fields are required and validated against the ranges supported by the app.

## GlobalSettings

| Field | JSON type | Constraints |
| --- | --- | --- |
| `appThemeMode` | String | One of `light`, `dark`, or `follow_system`. |
| `default01ColorLong` | Number | Unsigned 32-bit ARGB value. |
| `default04ColorLong` | Number | Unsigned 32-bit ARGB value. |
| `default07ColorLong` | Number | Unsigned 32-bit ARGB value. |
| `default10ColorLong` | Number | Unsigned 32-bit ARGB value. |

Color values are stored as JSON Long values in the range
`0x00000000` through `0xFFFFFFFF`.

## Version Compatibility

| Format version | Import | Export | Modification-time field | Status |
| --- | --- | --- | --- | --- |
| 1 | Supported | No | `lastBasicInfoModifiedAtMillis` | Historical format. |
| 2 | Supported | Yes | `lastModifiedAtMillis` | Current format. |

The current writer only produces version 2 backups. The current reader accepts
versions 1 and 2; versions outside that range are rejected before stored data is changed.

When required fields are added, removed, renamed, or given different semantics,
`formatVersion` must be incremented. Readers should retain explicit conversion logic
for every older supported version instead of silently reinterpreting changed fields.

## Import Behavior

1. The complete JSON document is parsed and validated before any stored data is changed.
2. Bangumi and Schedule rows replace the current Room contents in a transaction.
3. Calendar and global settings replace the current DataStore settings in one edit.
4. Imported IDs and modification timestamps are preserved.
5. `expectedEndDate` is recalculated from the validated imported data.
