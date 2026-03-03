package bg.iag.tel24.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class CacheDao_Impl implements CacheDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CachedData> __insertionAdapterOfCachedData;

  public CacheDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCachedData = new EntityInsertionAdapter<CachedData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `cache` (`key`,`json`,`timestamp`) VALUES (?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, CachedData value) {
        if (value.getKey() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getKey());
        }
        if (value.getJson() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getJson());
        }
        stmt.bindLong(3, value.getTimestamp());
      }
    };
  }

  @Override
  public Object put(final CachedData data, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCachedData.insert(data);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object get(final String key, final Continuation<? super CachedData> continuation) {
    final String _sql = "SELECT * FROM cache WHERE `key` = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (key == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, key);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CachedData>() {
      @Override
      public CachedData call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfJson = CursorUtil.getColumnIndexOrThrow(_cursor, "json");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final CachedData _result;
          if(_cursor.moveToFirst()) {
            final String _tmpKey;
            if (_cursor.isNull(_cursorIndexOfKey)) {
              _tmpKey = null;
            } else {
              _tmpKey = _cursor.getString(_cursorIndexOfKey);
            }
            final String _tmpJson;
            if (_cursor.isNull(_cursorIndexOfJson)) {
              _tmpJson = null;
            } else {
              _tmpJson = _cursor.getString(_cursorIndexOfJson);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _result = new CachedData(_tmpKey,_tmpJson,_tmpTimestamp);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
